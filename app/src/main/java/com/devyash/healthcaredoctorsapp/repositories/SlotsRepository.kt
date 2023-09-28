package com.devyash.healthcaredoctorsapp.repositories

import com.devyash.healthcaredoctorsapp.models.SlotList
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.util.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SlotsRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    val currentUser = firebaseAuth?.currentUser

    suspend fun addSlotToFirebase(
        slotTimings: SlotList,
        slotPosition: Int
    ): Flow<NetworkResult<String>> {
        return flow {
            val doctorId = currentUser?.uid.toString()
            val timingsMap = mapOf(
                "timings" to slotTimings.timings
            )

            firestore.collection("Doctors").document(doctorId).collection("Slots")
                .document(slotPosition.toString()).set(timingsMap).await()
            emit(NetworkResult.Success("Slot Added"))
        }.catch {
            NetworkResult.Error(it.message, null)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getAllSlots(): Flow<NetworkResult<MutableList<SlotList>>> {
        return flow {
            val slotCollectionRef =
                firestore.collection("Doctors").document(firebaseAuth.uid.toString())
                    .collection("Slots")

            val querySnapshot = slotCollectionRef.get().await()
            val listOfSlots = mutableListOf<SlotList>()

            for (document in querySnapshot) {
                if (document.exists()) {
                    val slotList = SlotList(
                        timings =document.getString("timings") ?: ""
                    )
                    listOfSlots.add(slotList)
                }
            }
            emit(NetworkResult.Success(listOfSlots))
        }.catch {
            NetworkResult.Error(it.message, null)
        }.flowOn(Dispatchers.IO)
    }

}