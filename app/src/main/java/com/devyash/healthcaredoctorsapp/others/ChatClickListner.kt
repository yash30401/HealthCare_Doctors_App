package com.devyash.healthcaredoctorsapp.others

import com.devyash.healthcaredoctorsapp.models.DetailedDoctorAppointment

interface ChatClickListner {
    fun onClick(doctorAppointment: DetailedDoctorAppointment)
}