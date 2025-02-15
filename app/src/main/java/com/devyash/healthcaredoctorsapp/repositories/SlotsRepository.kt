package com.devyash.healthcaredoctorsapp.repositories

import android.util.Log
import com.devyash.healthcaredoctorsapp.models.SlotList
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.util.await
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.sql.Time
import javax.inject.Inject

class SlotsRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    val currentUser = firebaseAuth?.currentUser

    suspend fun addSlotToFirebase(
        slotTimings: SlotList,
    ): Flow<NetworkResult<String>> {
        return flow {
            val doctorId = currentUser?.uid.toString()

            // Converting Long To Timestamp
            val timestampObject = Timestamp(java.util.Date(slotTimings.timings))
            Log.d("TIMECHECKING","TimeStamp in Repo:- ${slotTimings.timings}")
            val truncatedTimestamp = Timestamp(timestampObject.seconds - timestampObject.seconds % 60,timestampObject.nanoseconds)

            val tolerance = 1000

            val existingSlotQuery = firestore.collection("Doctors").document(doctorId).collection("Slots")
                .whereGreaterThanOrEqualTo("timings", Timestamp(truncatedTimestamp.seconds - tolerance, 0))
                .whereLessThanOrEqualTo("timings", Timestamp(truncatedTimestamp.seconds + tolerance, 0))

            val existingSlotSnapshot = existingSlotQuery.get().await()

            if(existingSlotSnapshot.isEmpty){
                val timingsMap = mapOf("timings" to truncatedTimestamp)

                firestore.collection("Doctors").document(doctorId).collection("Slots").add(timingsMap).await()

                emit(NetworkResult.Success("Slot Added"))
            }else{
                emit(NetworkResult.Error("Slot with the same timing already exists"))
            }
        }.catch {
            NetworkResult.Error(it.message, null)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getAllSlots(): Flow<NetworkResult<MutableList<Long>>> {
        return flow {
            val slotCollectionRef =
                firestore.collection("Doctors").document(firebaseAuth.uid.toString())
                    .collection("Slots")

            val querySnapshot = slotCollectionRef.get().await()
            val listOfTimestamps = mutableListOf<Long>()

            for (document in querySnapshot) {
                if (document.exists()) {
                    val timestamp = document.getTimestamp("timings")
                    val timestampInMillis = timestamp?.toDate()?.time ?: 0L

                    listOfTimestamps.add(timestampInMillis)
                }
            }
            emit(NetworkResult.Success(listOfTimestamps))
        }.catch {
            NetworkResult.Error(it.message, null)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteSlot(slotTiming:Long):Flow<NetworkResult<String>>{
        return flow {
            val timestampObject = Timestamp(java.util.Date(slotTiming))
            val truncatedTimestamp = Timestamp(
                timestampObject.seconds - timestampObject.seconds % 60,
                timestampObject.nanoseconds
            )
            val tolerance = 1000 // 1 second tolerance (adjust as needed)

            val slotCollectionRef = firestore.collection("Doctors").document(firebaseAuth.currentUser?.uid.toString())
                .collection("Slots")

            val query = slotCollectionRef
                .whereGreaterThanOrEqualTo("timings", Timestamp(truncatedTimestamp.seconds - tolerance, 0))
                .whereLessThanOrEqualTo("timings", Timestamp(truncatedTimestamp.seconds + tolerance, 0))


            val querySnapshot = query.get().await()

            for (document in querySnapshot) {
                slotCollectionRef.document(document.id).delete().await()
            }

            emit(NetworkResult.Success("Slot(s) Deleted"))
        }.catch {
            NetworkResult.Error(it.message.toString(),null)
        }.flowOn(Dispatchers.IO)
    }


}