package com.devyash.healthcaredoctorsapp.repositories

import android.util.Log
import com.devyash.healthcaredoctorsapp.models.DoctorData
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.others.Constants
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

    suspend fun checkIfUserAlreadyExist(phoneNumber:String):Flow<NetworkResult<Boolean>>{
        return flow<NetworkResult<Boolean>> {
            val userExist = firebaseAuth.fetchSignInMethodsForEmail(phoneNumber).await()
            val signInMethods = userExist?.signInMethods
            if(signInMethods!=null && signInMethods.isNotEmpty()){
                emit(NetworkResult.Success(true))
                Log.d(Constants.FIRESTOREDATASTATUS,"Emiting true")
            }else{
                Log.d(Constants.FIRESTOREDATASTATUS,"Emiting true")
                emit(NetworkResult.Success(false))
            }
        }.catch {e->
            Log.d(Constants.FIRESTOREDATASTATUS,e.message.toString())
            emit(NetworkResult.Error(e.message,null))
        }
    }
    
    suspend fun signinWithPhoneNumber(credential: PhoneAuthCredential): Flow<NetworkResult<out FirebaseUser>> {
        return flow {
            val result = firebaseAuth.signInWithCredential(credential).await()
            emit(NetworkResult.Success(result.user!!))
        }.catch { e ->
            NetworkResult.Error(e.message,null)
        }
    }

    suspend fun addDoctorDataToFirebase(data: DoctorData): Flow<NetworkResult<String>> {
        return flow {
            firebaseFirestore.collection("Doctors").document(firebaseAuth.uid.toString()).set(data).await()
            emit(NetworkResult.Success("Data Added"))
        }.catch { e ->
            NetworkResult.Error(e.message,null)
        }
    }

    fun logout(){
        firebaseAuth.signOut()
    }

}