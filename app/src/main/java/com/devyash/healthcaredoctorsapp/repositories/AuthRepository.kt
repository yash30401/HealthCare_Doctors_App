package com.devyash.healthcaredoctorsapp.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import javax.inject.Inject

class AuthRepository @Inject constructor(private val firebaseAuth: FirebaseAuth) {

    val currentUser:FirebaseUser?= firebaseAuth.currentUser

    suspend fun signinWithPhoneNumber(credential: PhoneAuthCredential):

}