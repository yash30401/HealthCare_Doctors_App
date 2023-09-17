package com.devyash.healthcaredoctorsapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devyash.healthcaredoctorsapp.models.DoctorData
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.repositories.AuthRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private var repository: AuthRepository) : ViewModel() {

    private val _userExistFlow = MutableStateFlow<NetworkResult<Boolean>?>(null)
    val userExistFlow: StateFlow<NetworkResult<Boolean>?> = _userExistFlow

    private val _loginFlow = MutableStateFlow<NetworkResult<FirebaseUser>?>(null)
    val loginFlow: StateFlow<NetworkResult<FirebaseUser>?> = _loginFlow

    private val _doctorDataFlow = MutableStateFlow<NetworkResult<String>?>(null)
    val doctorDataFlow:StateFlow<NetworkResult<String>?> = _doctorDataFlow

    val currentUser: FirebaseUser? get() = repository.currentUser

    init {
        if (repository.currentUser != null) {
            _loginFlow.value = NetworkResult.Success(repository.currentUser!!)
        }
    }

    fun checkIfUserAlreadyExist() = viewModelScope.launch {
        _userExistFlow.value = NetworkResult.Loading()

        try {
            val result = repository.checkIfUserAlreadyExist()
            result.catch { e->
                _userExistFlow.value = NetworkResult.Error(e.message)
            }.collect{data->
                _userExistFlow.value = NetworkResult.Success(data.data!!)
            }
        }catch (e:Exception){
            _userExistFlow.value = NetworkResult.Error(e.message)
        }
    }
    fun signInWithPhoneNumber(credential: PhoneAuthCredential) = viewModelScope.launch {
        _loginFlow.value = NetworkResult.Loading()

        val result = repository.signinWithPhoneNumber(credential)
        result.collect { data ->
            when (data) {
                is NetworkResult.Error -> {
                    _loginFlow.value = NetworkResult.Error(data.message, null)
                }
                is NetworkResult.Success -> {
                    _loginFlow.value = NetworkResult.Success(data.data!!)
                }

                else -> {}
            }
        }
    }


    fun addDoctorDataToFirestore(data: DoctorData) = viewModelScope.launch {
        _doctorDataFlow.value = NetworkResult.Loading()

        val result = repository.addDoctorDataToFirebase(data)
        result.collect {
            when (it) {
                is NetworkResult.Error -> {
                    _doctorDataFlow.value = NetworkResult.Error(it.message)
                }
                is NetworkResult.Success -> {
                    _doctorDataFlow.value = NetworkResult.Success(it.data.toString())
                }

                else -> {}
            }
        }
    }


    fun signout(){
        repository.logout()
        _loginFlow.value = null
    }
}