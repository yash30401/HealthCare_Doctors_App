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

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private lateinit var listOfServices: MutableList<String>
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

        binding.btnRegister.setOnClickListener {
            validateFields()
        }

    }

    private fun validateFields() {
        val etMobileNo = binding.etMobileNo.text.toString()
        val fullName = binding.tilFullNameLayout.editText?.text.toString()
        val specialization = binding.tilSpecializationLayout.editText?.text.toString()
        val about = binding.tilAboutLayout.editText?.text.toString()
        val city = binding.tilCityLayout.editText?.text.toString()
        val address = binding.tilAddressLayout.editText?.text.toString()
        val experience = binding.tilExperienceLayout.editText?.text.toString()

        //Optional
        val email = binding.tilEmailLayout.editText?.text.toString()
        val website = binding.tilWebsiteLayout.editText?.text.toString()

        val servicesSize = listOfServices.size
        val workingHours = binding.tilWorkingHoursLayout.editText?.text.toString()
        val clinicVisit = binding.tilClinicVisitLayout.editText?.text.toString()
        val videoConsult = binding.tilVideoConsultLayout.editText?.text.toString()



        if (etMobileNo.isNullOrEmpty() || fullName.isNullOrEmpty() || specialization.isNullOrEmpty() || about.isNullOrEmpty()
            || city.isNullOrEmpty() || address.isNullOrEmpty() || experience.isNullOrEmpty() || workingHours.isNullOrEmpty() || clinicVisit.isNullOrEmpty() || videoConsult.isNullOrEmpty()
        ) {
            Toast.makeText(requireContext(), "Empty Fields!", Toast.LENGTH_SHORT).show()
        } else if (servicesSize == 0) {
            Toast.makeText(requireContext(), "Please Add Services", Toast.LENGTH_SHORT).show()
        } else {
            registerDoctor()
        }
    }

    private fun registerDoctor() {
        Toast.makeText(requireContext(), "Successfull", Toast.LENGTH_SHORT).show()
    }


    // Adding Chips programmatically to add services
    private fun addChip() {
        val service = binding.tilServicesLayout.editText?.text.toString()

        if (service == "") {
            Toast.makeText(requireContext(), "Please Enter Service", Toast.LENGTH_SHORT).show()
        } else {
            val chip = Chip(requireContext())
            chip.setText(service)
            chip.chipStrokeWidth = 0F
            chip.setChipBackgroundColorResource(R.color.specialistCardBackgroundColor)
            chip.chipCornerRadius = 20F
            chip.isCloseIconVisible = true
            chip.closeIcon =
                ContextCompat.getDrawable(requireContext(), R.drawable.baseline_delete_24)

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