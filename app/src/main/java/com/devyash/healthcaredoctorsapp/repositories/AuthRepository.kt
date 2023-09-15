package com.devyash.healthcaredoctorsapp.repositories

import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import javax.inject.Inject

class AuthRepository @Inject constructor(private val firebaseAuth: FirebaseAuth) {

    val currentUser:FirebaseUser?= firebaseAuth.currentUser

    suspend fun signinWithPhoneNumber(credential: PhoneAuthCredential):NetworkResult<FirebaseUser>{
        return try{
            val result = firebaseAuth.signInWithCredential(credential)
            NetworkResult.Success()
        }catch (e:Exception){
            NetworkResult.Error(e.message)
        }
    }

}