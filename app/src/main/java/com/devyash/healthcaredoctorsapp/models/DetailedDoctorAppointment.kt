package com.devyash.healthcaredoctorsapp.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import java.io.Serializable

data class DetailedDoctorAppointment(
    val status:String, val typeOfConsultation:String,
    val dateTime: Timestamp?, val userReference: DocumentReference
):Serializable