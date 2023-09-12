package com.devyash.healthcaredoctorsapp.others

import com.google.firebase.auth.PhoneAuthProvider

sealed class PhoneAuthCallbackSealedClass(
    val verificationId: String?,
    val token: PhoneAuthProvider.ForceResendingToken?,
    val firebaseException: String?,
    val firebaseAuthInvalidCredentialsException: String?,
    val firebaseTooManyRequestsException: String?,
    val firebaseAuthMissingActivityForRecaptchaException: String?
) {
    class ONVERIFICATIONCOMPLETED : PhoneAuthCallbackSealedClass(
        null,
        null,
        null,
        null,
        null,
        null
    )

    class ONVERIFICATIONFAILED(firebaseException: String?) :
        PhoneAuthCallbackSealedClass(
            null,
            null, firebaseException,
            null,
            null,
            null
        )

    class FIREBASEAUTHINVALIDCREDENTIALSEXCEPTION(firebaseAuthInvalidCredentialsException: String?) :
        PhoneAuthCallbackSealedClass(
            null,
            null,
            null,
            firebaseAuthInvalidCredentialsException,
            null,
            null
        )

    class FIREBASETOOMANYREQUESTSEXCEPTION(firebaseTooManyRequestsException: String?) :
        PhoneAuthCallbackSealedClass(
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
        PhoneAuthCallbackSealedClass(
            null,
            null,
            null,
            null,
            null,
            firebaseAuthMissingActivityForRecaptchaException
        )

    class ONCODESENT(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) :
        PhoneAuthCallbackSealedClass(
            verificationId,
            token,
            null,
            null,
            null,
            null
        )
}