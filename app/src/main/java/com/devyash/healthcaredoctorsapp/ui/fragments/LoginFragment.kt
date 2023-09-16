package com.devyash.healthcaredoctorsapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devyash.healthcaredoctorsapp.R
import com.devyash.healthcaredoctorsapp.databinding.FragmentLoginBinding


class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding:FragmentLoginBinding?=null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}