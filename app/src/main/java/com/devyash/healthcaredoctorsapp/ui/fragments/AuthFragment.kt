package com.devyash.healthcaredoctorsapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.devyash.healthcaredoctorsapp.R
import com.devyash.healthcaredoctorsapp.databinding.FragmentAuthBinding
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AuthFragment : Fragment(R.layout.fragment_auth) {

    private var _binding:FragmentAuthBinding?=null
    private val binding get() = _binding!!

    private lateinit var listOfServices:MutableList<String>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAuthBinding.bind(view)

        binding.btnAddService.setOnClickListener {
            val service = binding.tilServicesLayout.editText?.text.toString()
            val chip  = Chip(requireContext())
            chip.setText(service)
            chip.chipStrokeWidth = 0F
            chip.setChipBackgroundColorResource(R.color.specialistCardBackgroundColor)
            chip.chipIcon = ContextCompat.getDrawable(requireContext(),R.drawable.baseline_delete_24)

            binding.chipGroupServices.addView(chip)
            listOfServices.add(service)
            binding.tilServicesLayout.editText?.text?.clear()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}