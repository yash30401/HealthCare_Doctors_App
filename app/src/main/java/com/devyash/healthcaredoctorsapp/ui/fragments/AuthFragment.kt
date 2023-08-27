package com.devyash.healthcaredoctorsapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

        listOfServices = emptyList<String>().toMutableList()

        binding.btnAddService.setOnClickListener {
            addChip()
        }

    }

    private fun addChip() {
        val service = binding.tilServicesLayout.editText?.text.toString()

        if(service == ""){
            Toast.makeText(requireContext(), "Please Enter Service", Toast.LENGTH_SHORT).show()
        }else{
            val chip  = Chip(requireContext())
            chip.setText(service)
            chip.chipStrokeWidth = 0F
            chip.setChipBackgroundColorResource(R.color.specialistCardBackgroundColor)
            chip.chipCornerRadius = 20F
            chip.isCloseIconVisible = true
            chip.closeIcon = ContextCompat.getDrawable(requireContext(),R.drawable.baseline_delete_24)

            binding.chipGroupServices.addView(chip)
            listOfServices.add(service)
            binding.tilServicesLayout.editText?.text?.clear()

            chip.setOnCloseIconClickListener {
                binding.chipGroupServices.removeView(chip)
                listOfServices.remove(service)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}