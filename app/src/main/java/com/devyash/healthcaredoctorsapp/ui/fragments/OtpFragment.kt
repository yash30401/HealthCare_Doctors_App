package com.devyash.healthcaredoctorsapp.ui.fragments

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.devyash.healthcaredoctorsapp.R
import com.devyash.healthcaredoctorsapp.databinding.FragmentOtpBinding
import com.devyash.healthcaredoctorsapp.utils.PhoneAuthCallback
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class OtpFragment : Fragment(R.layout.fragment_otp) {

    private var _binding:FragmentOtpBinding?=null
    private val binding get() = _binding!!

    private val args:OtpFragmentArgs  by navArgs()

    private lateinit var countDownTimer: CountDownTimer
    var isTimerRunning:Boolean? = false
    var currentCounterTimeInMilliSeconds = 0L

    private lateinit var phoneNumber:String

    @Inject
    lateinit var phoneAuthCallback: PhoneAuthCallback
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOtpBinding.bind(view)

        callbacks = phoneAuthCallback.callbacks

        setupPhoneNumberTextView()
        startOtpResendTimer()

        binding.tvPhoneNo.setOnClickListener {
            editPhoneNumberAndNavigateBackToAuthScreen()
        }

        binding.ivEditPhoneNo.setOnClickListener {
            editPhoneNumberAndNavigateBackToAuthScreen()
        }

        binding.btnVerifyOtp.setOnClickListener {
            val otp = binding.etOtpPin.editableText.toString()
            val verificationId = args.verificationId
            if(otp!=""){
                val credentials = PhoneAuthProvider.getCredential(
                    verificationId,
                    otp
                )
                binding.progressBar.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    siginWithPhoneNumber(credentials)
                }
            }else{
                Toast.makeText(requireContext(), "Please Enter Otp!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPhoneNumberTextView(){
        phoneNumber = args.phoneNumber
        val hiddenPhoneNumberText = "+91${phoneNumber.get(3)}${phoneNumber.get(4)}******${phoneNumber.get(11)}${
            phoneNumber.get(12)
        }"
        binding.tvPhoneNo.text = hiddenPhoneNumberText
    }

    private fun editPhoneNumberAndNavigateBackToAuthScreen(){
        findNavController().navigate(R.id.action_otpFragment_to_authFragment)
    }

    private fun startOtpResendTimer() {
        TODO("Not yet implemented")
    }

    private fun siginWithPhoneNumber(credentials: PhoneAuthCredential) {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}