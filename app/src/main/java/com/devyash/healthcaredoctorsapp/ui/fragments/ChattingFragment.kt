package com.devyash.healthcaredoctorsapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devyash.healthcaredoctorsapp.R
import com.devyash.healthcaredoctorsapp.databinding.FragmentChattingBinding


class ChattingFragment : Fragment(R.layout.fragment_chatting) {
    private var _binding:FragmentChattingBinding?=null
    private val binding get()= _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChattingBinding.bind(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding  = null
    }
}