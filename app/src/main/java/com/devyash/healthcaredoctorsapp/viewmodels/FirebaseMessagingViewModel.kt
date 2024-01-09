package com.devyash.healthcaredoctorsapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.repositories.FirebaseMessagingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirebaseMessagingViewModel @Inject constructor(
    private val firebaseMessagingRepository: FirebaseMessagingRepository
):ViewModel() {

    private val _token = MutableStateFlow<NetworkResult<String>?>(null)
    val token: StateFlow<NetworkResult<String>?> = _token
    fun getFCMToken() = viewModelScope.launch {
        _token.value = NetworkResult.Loading()

        try {
            val result = firebaseMessagingRepository.getFCMToken()
            result.collect{
                when(it){
                    is NetworkResult.Error -> _token.value = NetworkResult.Error(it.message)
                    is NetworkResult.Loading -> _token.value = NetworkResult.Loading()
                    is NetworkResult.Success -> _token.value = NetworkResult.Success(it.data!!)
                }
            }
        }catch (
        e:Exception){
            _token.value = NetworkResult.Error(e.message)
        }
    }
}