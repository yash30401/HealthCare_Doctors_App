package com.devyash.healthcaredoctorsapp.repositories

import com.devyash.healthcaredoctorsapp.models.DetailedDoctorAppointment
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

class AppointmentRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun getAllUpcomingAppointments(): Flow<NetworkResult<MutableList<DetailedDoctorAppointment>>> {
        return flow {
            val appointmentCollectionRef =
                firestore.collection("Doctors").document(firebaseAuth.currentUser?.uid.toString())
                    .collection("Appointments")

            try {
                val querySnapshot = appointmentCollectionRef.get().await()
                val listOfAppointments = mutableListOf<DetailedDoctorAppointment>()

                for (document in querySnapshot) {
                    if (document.exists()) {
                        val detailedDoctorAppointment = DetailedDoctorAppointment(
                            status = document.getString("status") ?: "",
                            typeOfConsultation = document.getString("typeOfConsultation") ?: "",
                            dateTime = document.getTimestamp("dateTime")!!
                        )

                        listOfAppointments.add(detailedDoctorAppointment)
                    }
                }
                emit(NetworkResult.Success(listOfAppointments))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message.toString()))
            }

        }.catch {
            NetworkResult.Error(it.message.toString(),null)
        }.flowOn(Dispatchers.IO)
    }

}