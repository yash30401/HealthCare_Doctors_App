package com.devyash.healthcaredoctorsapp.models

import java.io.Serializable

data class DoctorData(
    val Id:String= "",
    val About: String = "",
                         val Address: String = "",
                         val City: String = "",
                         val video_consult: Int? = 0,
                         val clinic_visit:Int?=0,
                         var Contact_Info: ContactInfo? = null,
                         val Experience: Int = 0,
                         val Name: String = "",
                         val Profile_Pic: String = "",
                         var Reviews_And_Ratings: List<ReviewsAndRatings>? = emptyList(),
                         val Services: List<String> = emptyList(),
                         val Specialization: String = "",
                         val Working_Hours: String = ""
): Serializable

data class ContactInfo(
    val email: String? = "",
    val phone_number: String = "",
    val website: String? = ""
)

data class ReviewsAndRatings(
    val date:String? = "",
    val name:String? ="",
    val rating: Double? = 0.0,
    val review: String? = ""
)