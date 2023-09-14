package com.devyash.healthcaredoctorsapp.utils

import com.devyash.healthcaredoctorsapp.others.PhoneAuthCallbackSealedClass
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class PhoneAuthCallback @Inject constructor() {
    private val _callbackFlow:MutableStateFlow<PhoneAuthCallbackSealedClass?> = MutableStateFlow(null)
    val callbackFlow:StateFlow<PhoneAuthCallbackSealedClass?> =_callbackFlow

    val callbacks =  object:PhoneAuthProvider.
}