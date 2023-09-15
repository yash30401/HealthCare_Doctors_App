package com.devyash.healthcaredoctorsapp.others

import com.devyash.healthcaredoctorsapp.utils.PhoneAuthCallback
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken

sealed class PhoneAuthCallBackSealedClass(
    val verificationId: String?,
    val token: ForceResendingToken?,
    val firebaseException: String?,
    val firebaseAuthInvalidCredentialsException: String?,
    val firebaseTooManyRequestsException: String?,
    val firebaseAuthMissingActivityForRecaptchaException: String?
) {
    class ONVERIFICATIONCOMPLETED : PhoneAuthCallBackSealedClass(
        null,
        null,
        null,
        null,
        null,
        null
    )

    class ONVERIFICATIONFAILED(firebaseException: String?) :
        PhoneAuthCallBackSealedClass(
            null,
            null, firebaseException,
            null,
            null,
            null
        )

    class FIREBASEAUTHINVALIDCREDENTIALSEXCEPTION(firebaseAuthInvalidCredentialsException: String?) :
        PhoneAuthCallBackSealedClass(
            null,
            null,
            null,
            firebaseAuthInvalidCredentialsException,
            null,
            null
        )

    class FIREBASETOOMANYREQUESTSEXCEPTION(firebaseTooManyRequestsException: String?) :
        PhoneAuthCallBackSealedClass(
            null,
            null,
            null,
            null,
            firebaseTooManyRequestsException,
            null
        )

    class FIREBASEAUTHMISSINGACTIVITYFORRECAPTCHAEXCEPTION(
        firebaseAuthMissingActivityForRecaptchaException: String?
    ) :
        PhoneAuthCallBackSealedClass(
            null,
            null,
            null,
            null,
            null,
            firebaseAuthMissingActivityForRecaptchaException
        )

    class ONCODESENT(verificationId: String, token: ForceResendingToken) :
        PhoneAuthCallBackSealedClass(
            verificationId,
            token,
            null,
            null,
            null,
            null
        )
}