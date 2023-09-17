package com.devyash.healthcaredoctorsapp.repositories

import android.util.Log
import com.devyash.healthcaredoctorsapp.models.DoctorData
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.others.Constants
import com.devyash.healthcaredoctorsapp.util.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AuthRepository @Inject constructor(private val firebaseAuth: FirebaseAuth, private val firebaseFirestore: FirebaseFirestore) {

    val currentUser:FirebaseUser?= firebaseAuth.currentUser
    suspend fun checkIfUserAlreadyExist(): Flow<NetworkResult<Boolean>> {
        return flow<NetworkResult<Boolean>> {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                // User is not authenticated, return false
                emit(NetworkResult.Success(false))
                return@flow
            }

            val uid = currentUser.uid

            // Check if the UID document exists in the "Doctors" collection
            val documentSnapshot = firebaseFirestore.collection("Doctors")
                .document(uid)
                .get()
                .await()

            val userExists = documentSnapshot.exists()

            emit(NetworkResult.Success(userExists))
            Log.d(Constants.FIRESTOREDATASTATUS, "Emitting: $userExists")
        }.catch { e ->
            Log.d(Constants.FIRESTOREDATASTATUS, e.message.toString())
            emit(NetworkResult.Error(e.message, null))
        }.flowOn(Dispatchers.IO)
    }

    
    suspend fun signinWithPhoneNumber(credential: PhoneAuthCredential): Flow<NetworkResult<out FirebaseUser>> {
        return flow {
            val result = firebaseAuth.signInWithCredential(credential).await()
            emit(NetworkResult.Success(result.user!!))
        }.catch { e ->
            NetworkResult.Error(e.message,null)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun addDoctorDataToFirebase(data: DoctorData): Flow<NetworkResult<String>> {
        return flow {
            firebaseFirestore.collection("Doctors").document(firebaseAuth.uid.toString()).set(data).await()
            emit(NetworkResult.Success("Data Added"))
        }.catch { e ->
            NetworkResult.Error(e.message,null)
        }.flowOn(Dispatchers.IO)
    }

    fun logout(){
        firebaseAuth.signOut()
    }

}