package com.devyash.healthcaredoctorsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.devyash.healthcaredoctorsapp.R
import com.devyash.healthcaredoctorsapp.databinding.FragmentAuthBinding
import com.devyash.healthcaredoctorsapp.models.ContactInfo
import com.devyash.healthcaredoctorsapp.models.DoctorData
import com.devyash.healthcaredoctorsapp.models.ResendTokenModelClass
import com.devyash.healthcaredoctorsapp.models.ReviewsAndRatings
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.others.Constants
import com.devyash.healthcaredoctorsapp.others.Constants.AUTHVERIFICATIONTAG
import com.devyash.healthcaredoctorsapp.others.Constants.FACEBOOKTEST
import com.devyash.healthcaredoctorsapp.others.Constants.FIRESTOREDATASTATUS
import com.devyash.healthcaredoctorsapp.others.PhoneAuthCallBackSealedClass
import com.devyash.healthcaredoctorsapp.others.PhoneNumberValidation
import com.devyash.healthcaredoctorsapp.utils.PhoneAuthCallback
import com.devyash.healthcaredoctorsapp.viewmodels.AuthViewModel
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class AuthFragment : Fragment(R.layout.fragment_auth) {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private lateinit var listOfServices: MutableList<String>

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var phoneAuthCallback: PhoneAuthCallback

    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private lateinit var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private val viewmodel:AuthViewModel by viewModels<AuthViewModel>()

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

        callback = phoneAuthCallback.callbacks
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



        if (fullName.isNullOrEmpty() || specialization.isNullOrEmpty() || about.isNullOrEmpty()
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
        val phoneNumberValidation = validatePhoneNumber(binding.etMobileNo.text.toString())
        phoneNoEventsHandle(phoneNumberValidation)
    }

    private fun validatePhoneNumber(number: String): PhoneNumberValidation =
        if (number.isEmpty()) PhoneNumberValidation.EMPTY else PhoneNumberValidation.SUCCESS

    private fun phoneNoEventsHandle(phoneNumberValidation: PhoneNumberValidation) {
        when (phoneNumberValidation) {
            PhoneNumberValidation.SUCCESS -> {
                binding.progressBar.visibility = View.VISIBLE
                sendVerificationCodeToPhoneNumber()
            }

            PhoneNumberValidation.EMPTY -> {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Please Enter Your Mobile Number", Toast.LENGTH_SHORT)
                    .show()
            }

            PhoneNumberValidation.WRONGFORMAT -> {
                binding.progressBar.visibility = View.GONE
            }
        }
    }


    private fun sendVerificationCodeToPhoneNumber() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        val phoneNumber =
            "${binding.etCountryCode.selectedCountryCodeWithPlus}${binding.etMobileNo.text.toString()}"

        val etMobileNo = binding.etMobileNo.text.toString()
        val fullName = binding.tilFullNameLayout.editText?.text.toString()
        val specialization = binding.tilSpecializationLayout.editText?.text.toString()
        val about = binding.tilAboutLayout.editText?.text.toString()
        val city = binding.tilCityLayout.editText?.text.toString()
        val address = binding.tilAddressLayout.editText?.text.toString()
        val experience = binding.tilExperienceLayout.editText?.text.toString().toInt()

        //Optional
        val email = binding.tilEmailLayout.editText?.text.toString()
        val website = binding.tilWebsiteLayout.editText?.text.toString()
        val contatcInfo = ContactInfo(email, phoneNumber, website)

        // Review and Rating
        val reviewsAndRatings = listOf<ReviewsAndRatings>(ReviewsAndRatings("", "", 0.0, ""))

        val servicesSize = listOfServices.size
        val workingHours = binding.tilWorkingHoursLayout.editText?.text.toString()
        val clinicVisit = binding.tilClinicVisitLayout.editText?.text.toString().toInt()
        val videoConsult = binding.tilVideoConsultLayout.editText?.text.toString().toInt()

        GlobalScope.launch(Dispatchers.IO) {
            phoneAuthCallback.callbackFlow?.collect {
                when (it) {
                    is PhoneAuthCallBackSealedClass.FIREBASEAUTHINVALIDCREDENTIALSEXCEPTION -> {
                        Log.d(
                            AUTHVERIFICATIONTAG,
                            "onVerificationFailed: ${it.firebaseAuthInvalidCredentialsException}"
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Invalid Credentials!", Toast.LENGTH_SHORT)
                                .show()
                            binding.progressBar.visibility = View.GONE
                            binding.btnRegister.isEnabled = true
                        }
                    }

                    is PhoneAuthCallBackSealedClass.FIREBASEAUTHMISSINGACTIVITYFORRECAPTCHAEXCEPTION -> {
                        Log.d(
                            AUTHVERIFICATIONTAG,
                            "onVerificationFailed: ${it.firebaseAuthMissingActivityForRecaptchaException}"
                        )
                        withContext(Dispatchers.IO) {
                            Toast.makeText(context, "reCaptcha Problem", Toast.LENGTH_SHORT).show()
                            binding.progressBar.visibility = View.GONE
                            binding.btnRegister.isEnabled = true
                        }
                    }

                    is PhoneAuthCallBackSealedClass.FIREBASETOOMANYREQUESTSEXCEPTION -> {
                        Log.d(
                            AUTHVERIFICATIONTAG,
                            "onVerificationFailed: ${it.firebaseTooManyRequestsException}"
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Too many requests", Toast.LENGTH_SHORT).show()
                            binding.progressBar.visibility = View.GONE
                            binding.btnRegister.isEnabled = true
                        }
                    }

                    is PhoneAuthCallBackSealedClass.ONCODESENT -> {
                        Log.d("AUTHVERIFICATION", "onCodeSent:${it.verificationId}")


                        suspendCancellableCoroutine { continuation ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val result = async {
                                    storedVerificationId = it.verificationId.toString()
                                    resendToken = it.token!!
                                }
                                continuation.resume(result.await(), {
                                    Log.d(FACEBOOKTEST, it.message.toString())
                                }) // Resume the coroutine with the result
                            }
                        }


                        val action =
                            AuthFragmentDirections.actionAuthFragmentToOtpFragment(
                                it.verificationId.toString(),
                                phoneNumber,
                                ResendTokenModelClass(resendToken),
                                DoctorData(
                                    about,
                                    address,
                                    city,
                                    videoConsult,
                                    clinicVisit,
                                    contatcInfo,
                                    experience,
                                    fullName,
                                    "",
                                    reviewsAndRatings,
                                    listOfServices,
                                    specialization,
                                    workingHours
                                ),
                                "Register"
                            )
                        withContext(Dispatchers.Main) {
                            findNavController().navigate(action)
                        }

                    }

                    is PhoneAuthCallBackSealedClass.ONVERIFICATIONCOMPLETED -> {
                        Log.d(AUTHVERIFICATIONTAG, "Verification Completed")
                    }

                    is PhoneAuthCallBackSealedClass.ONVERIFICATIONFAILED -> {
                        Log.d(AUTHVERIFICATIONTAG, "onVerificationFailed: ${it.firebaseException}")
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            binding.btnRegister.isEnabled = true
                        }
                    }

                    else -> {
                        Log.d(
                            AUTHVERIFICATIONTAG,
                            "Verification Error: ${it?.firebaseException}"
                        )
                    }
                }
            }
        }

        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callback)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
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