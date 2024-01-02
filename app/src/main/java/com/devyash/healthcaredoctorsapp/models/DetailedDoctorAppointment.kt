package com.devyash.healthcaredoctorsapp.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class DetailedDoctorAppointment (val status:String, val typeOfConsultation:String,
                                 val dateTime: Timestamp, val userReference:DocumentReference
)