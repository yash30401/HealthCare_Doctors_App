package com.devyash.healthcaredoctorsapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devyash.healthcaredoctorsapp.models.SlotList
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.repositories.SlotsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SlotViewModel @Inject constructor(private var slotsRepository: SlotsRepository):ViewModel() {

    private val _slotFlow = MutableStateFlow<NetworkResult<String>?>(null)
    val slotFlow:StateFlow<NetworkResult<String>?> = _slotFlow

    private val _allSlotFlow= MutableStateFlow<NetworkResult<List<String>>?>(null)
    val allSlotFlow:StateFlow<NetworkResult<List<String>>?> = _allSlotFlow

    fun addSlotToFirebase(slotTimings:SlotList,slotPosition:Int) = viewModelScope.launch{
        _slotFlow.value = NetworkResult.Loading()

        val result = slotsRepository.addSlotToFirebase(slotTimings,slotPosition)
        result.collect{
            when(it){
                is NetworkResult.Error -> _slotFlow.value = NetworkResult.Error(it.message.toString())
                is NetworkResult.Loading -> _slotFlow.value = NetworkResult.Loading()
                is NetworkResult.Success -> _slotFlow.value = NetworkResult.Success(it.data.toString())
            }
        }
    }

    fun getAllSlots() = viewModelScope.launch {
        _allSlotFlow.value = NetworkResult.Loading()
        val result = slotsRepository.getAllSlots()
        result.collect{
            when(it){
                is NetworkResult.Error -> {
                    _allSlotFlow.value = NetworkResult.Error(it.message.toString())
                }
                is NetworkResult.Loading -> {
                    _allSlotFlow.value = NetworkResult.Loading()
                }
                is NetworkResult.Success -> {
                    _allSlotFlow.value = NetworkResult.Success(it.data?.toList()!!)
                }
            }
        }
    }
}