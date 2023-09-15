package com.devyash.healthcaredoctorsapp.utils

import android.util.Log
import com.devyash.healthcaredoctorsapp.others.Constants
import com.devyash.healthcaredoctorsapp.others.PhoneAuthCallBackSealedClass
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class PhoneAuthCallback @Inject constructor() {
    private val _callbackFlow:MutableStateFlow<PhoneAuthCallBackSealedClass?> = MutableStateFlow(null)
    val callbackFlow:StateFlow<PhoneAuthCallBackSealedClass?> =_callbackFlow

    val callbacks =  object:PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            _callbackFlow.value = PhoneAuthCallBackSealedClass.ONVERIFICATIONCOMPLETED()
        }

        override fun onVerificationFailed(e: FirebaseException) {
            _callbackFlow.value = PhoneAuthCallBackSealedClass.ONVERIFICATIONFAILED(e.message)

            if (e is FirebaseAuthInvalidCredentialsException) {
//                    Log.w(Constants.AUTHVERIFICATIONTAG, "onVerificationFailed", e)
//                    Toast.makeText(context, "Invalid Credentials!", Toast.LENGTH_SHORT).show()
                _callbackFlow.value =
                    PhoneAuthCallBackSealedClass.FIREBASEAUTHINVALIDCREDENTIALSEXCEPTION(e.message)
            } else if (e is FirebaseTooManyRequestsException) {
//                    Log.w(Constants.AUTHVERIFICATIONTAG, "onVerificationFailed", e)
//                    Toast.makeText(context, "Too many requests", Toast.LENGTH_SHORT).show()
                _callbackFlow.value =
                    PhoneAuthCallBackSealedClass.FIREBASETOOMANYREQUESTSEXCEPTION(e.message)
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
//                    Log.w(Constants.AUTHVERIFICATIONTAG, "onVerificationFailed", e)
//                    Toast.makeText(context, "reCaptcha Problem", Toast.LENGTH_SHORT).show()
                _callbackFlow.value =
                    PhoneAuthCallBackSealedClass.FIREBASEAUTHMISSINGACTIVITYFORRECAPTCHAEXCEPTION(e.message)
            }
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            Log.d(Constants.AUTHVERIFICATIONTAG,"codeSent")
            _callbackFlow.value = PhoneAuthCallBackSealedClass.ONCODESENT(verificationId, token)
        }

    }
}