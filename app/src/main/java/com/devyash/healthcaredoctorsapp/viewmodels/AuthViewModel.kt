package com.devyash.healthcaredoctorsapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.repositories.AuthRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private var repository: AuthRepository) : ViewModel() {

    private val _loginFlow = MutableStateFlow<NetworkResult<FirebaseUser>?>(null)
    val loginFlow: StateFlow<NetworkResult<FirebaseUser>?> = _loginFlow

    val currentUser: FirebaseUser? get() = repository.currentUser

    init {
        if (repository.currentUser != null) {
            _loginFlow.value = NetworkResult.Success(repository.currentUser!!)
        }
    }

    fun signInWithPhoneNumber(credential: PhoneAuthCredential) = viewModelScope.launch {
        _loginFlow.value = NetworkResult.Loading()

        try {
            val result = repository.signinWithPhoneNumber(credential)
            result.catch { e->
                _loginFlow.value = NetworkResult.Error(e.message)
            }.collect{data->
                _loginFlow.value = NetworkResult.Success(data.data!!)
            }
        }catch (e:Exception){
            _loginFlow.value = NetworkResult.Error(e.message)
        }
    }

    fun signout(){
        repository.logout()
        _loginFlow.value = null
    }
}