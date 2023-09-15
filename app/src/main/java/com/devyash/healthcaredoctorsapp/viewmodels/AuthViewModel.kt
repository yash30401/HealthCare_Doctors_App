package com.devyash.healthcaredoctorsapp.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private var repository:AuthRepository) :ViewModel() {

}