package com.devyash.healthcaredoctorsapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devyash.healthcaredoctorsapp.models.DetailedDoctorAppointment
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.repositories.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor(private val appointmentRepository: AppointmentRepository):ViewModel() {

    private val _upcomingAppointments = MutableStateFlow<NetworkResult<List<DetailedDoctorAppointment>>?>(null)
    val upcomingAppointments:StateFlow<NetworkResult<List<DetailedDoctorAppointment>>?> = _upcomingAppointments

    init{
        getAllUpcomingAppointments()
    }

    private fun getAllUpcomingAppointments() = viewModelScope.launch {
        _upcomingAppointments.value = NetworkResult.Loading()

        try {

            val result = appointmentRepository.getAllUpcomingAppointments()
            result.collect{
                when(it){
                    is NetworkResult.Error -> _upcomingAppointments.value = NetworkResult.Error(it.message.toString())
                    is NetworkResult.Loading -> _upcomingAppointments.value = NetworkResult.Loading()
                    is NetworkResult.Success -> _upcomingAppointments.value = NetworkResult.Success(it.data!!)
                }
            }
        }catch (e:Exception){
            _upcomingAppointments.value = NetworkResult.Error(e.message.toString())
        }
    }
}