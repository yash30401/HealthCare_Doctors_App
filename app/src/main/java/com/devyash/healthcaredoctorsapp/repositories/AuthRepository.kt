package com.devyash.healthcaredoctorsapp.repositories

import com.devyash.healthcaredoctorsapp.models.DoctorData
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.util.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepository @Inject constructor(private val firebaseAuth: FirebaseAuth, private val firebaseFirestore: FirebaseFirestore) {

    val currentUser:FirebaseUser?= firebaseAuth.currentUser

    suspend fun signinWithPhoneNumber(credential: PhoneAuthCredential): Flow<NetworkResult<FirebaseUser>> {
        return flow {
            try {
                val result = firebaseAuth.signInWithCredential(credential).await()
                emit(NetworkResult.Success(result.user!!))
            } catch (e: Exception) {
               emit(NetworkResult.Error(e.message))
            }
        }
    }

    suspend fun addDoctorDataToFirebase(data:DoctorData):Flow<NetworkResult<String>>{
        return flow {
            try {
                firebaseFirestore.collection("Doctors").document(firebaseAuth.uid.toString()).set(data).await()
                emit(NetworkResult.Success("Data Added"))
            }catch (e:Exception){
             emit(NetworkResult.Error(e.message))
            }
        }
    }

    fun logout(){
        firebaseAuth.signOut()
    }

}