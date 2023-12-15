package com.devyash.healthcaredoctorsapp.models

import com.google.firebase.Timestamp

data class DetailedDoctorAppointment (val status:String, val typeOfConsultation:String,
                                 val dateTime: Timestamp
)