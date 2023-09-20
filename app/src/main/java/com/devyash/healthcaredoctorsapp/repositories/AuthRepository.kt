package com.devyash.healthcaredoctorsapp.repositories

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.devyash.healthcaredoctorsapp.models.DoctorData
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.others.Constants
import com.devyash.healthcaredoctorsapp.others.Constants.STORAGEFAILURE
import com.devyash.healthcaredoctorsapp.util.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.URI
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val storageReference: StorageReference
) {

    // Get the current authenticated user (if any)
    val currentUser: FirebaseUser? = firebaseAuth.currentUser

    // Check if a user with the same UID already exists
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

    // Sign in with a phone number using the provided credentials
    suspend fun signinWithPhoneNumber(credential: PhoneAuthCredential): Flow<NetworkResult<out FirebaseUser>> {
        return flow {
            val result = firebaseAuth.signInWithCredential(credential).await()
            emit(NetworkResult.Success(result.user!!))
        }.catch { e ->
            NetworkResult.Error(e.message, null)
        }.flowOn(Dispatchers.IO)
    }



    // Add doctor data to Firebase Firestore
    suspend fun addDoctorDataToFirebase(data: DoctorData): Flow<NetworkResult<String>> {
        return flow {

            val doctorId = firebaseAuth.uid.toString()

            var imageUrl:String = ""
            val imageRef = storageReference.child("DoctorsProfilePicture/$doctorId.jpg")
             imageRef.putFile(data.Profile_Pic.toUri()).await()
             val url = imageRef.downloadUrl.await()
            imageUrl = url.toString()

            Log.d(STORAGEFAILURE,imageUrl)

                val doctorDataMap = mapOf(
                    "About" to data.About,
                    "Address" to data.Address,
                    "City" to data.City,
                    "video_consult" to data.video_consult,
                    "clinic_visit" to data.clinic_visit,
                    "Contact_Info" to mapOf(
                        "email" to data.Contact_Info?.email,
                        "phone_number" to data.Contact_Info?.phone_number,
                        "website" to data.Contact_Info?.website
                    ),
                    "Experience" to data.Experience,
                    "Name" to data.Name,
                    "Profile_Pic" to imageUrl,
                    "Reviews_and_Ratings" to data.Reviews_And_Ratings?.map { review ->
                        mapOf(
                            "date" to review.date,
                            "name" to review.name,
                            "rating" to review.rating,
                            "review" to review.review
                        )
                    },
                    "Services" to data.Services,
                    "Specialization" to data.Specialization,
                    "Working_Hours" to data.Working_Hours
                )

                firebaseFirestore.collection("Doctors").document(doctorId).set(doctorDataMap)
                    .await()

            emit(NetworkResult.Success("Data Added"))
        }.catch { e ->
            NetworkResult.Error(e.message, null)
        }.flowOn(Dispatchers.IO)
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    suspend fun deleteAccount(){
        firebaseAuth.currentUser?.delete()?.await()
    }

}