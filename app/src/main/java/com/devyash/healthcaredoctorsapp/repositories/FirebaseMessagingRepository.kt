package com.devyash.healthcaredoctorsapp.repositories

import com.devyash.healthcaredoctorsapp.models.FirebaseMessagingData
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.util.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class FirebaseMessagingRepository @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun getFCMToken(): Flow<NetworkResult<String>> {
        return flow{
            try {
                val token = firebaseMessaging.token.await()

                firestore.collection("Doctors").document(firebaseAuth.currentUser?.uid.toString())
                    .collection("FirebaseMessaging").document("token").set(FirebaseMessagingData(token)).await()

                emit(NetworkResult.Success(token.toString()))
            }catch (e:Exception){
                emit(NetworkResult.Error(e.message))
            }
        }.catch {
            NetworkResult.Error(it.message.toString(),null)
        }.flowOn(Dispatchers.IO)
    }


}