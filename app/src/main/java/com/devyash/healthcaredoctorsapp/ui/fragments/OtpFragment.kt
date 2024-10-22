package com.devyash.healthcaredoctorsapp.ui.fragments

import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.os.CountDownTimer
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
import androidx.navigation.fragment.navArgs
import com.devyash.healthcaredoctorsapp.R
import com.devyash.healthcaredoctorsapp.databinding.FragmentOtpBinding
import com.devyash.healthcaredoctorsapp.models.DoctorData
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.others.Constants
import com.devyash.healthcaredoctorsapp.others.Constants.COUNTDOWNTIMEINMINUTE
import com.devyash.healthcaredoctorsapp.others.Constants.FIRESTOREDATASTATUS
import com.devyash.healthcaredoctorsapp.others.Constants.TAG
import com.devyash.healthcaredoctorsapp.utils.PhoneAuthCallback
import com.devyash.healthcaredoctorsapp.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class OtpFragment : Fragment(R.layout.fragment_otp) {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!

    private val args: OtpFragmentArgs by navArgs()

    // Variables for OTP timer
    private lateinit var countDownTimer: CountDownTimer
    var isTimerRunning: Boolean? = false
    var currentCounterTimeInMilliSeconds = 0L

    private lateinit var phoneNumber: String

    @Inject
    lateinit var phoneAuthCallback: PhoneAuthCallback
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private val viewModel: AuthViewModel by viewModels<AuthViewModel>()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

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

        // Set up UI elements and listeners
        setupPhoneNumberTextView()
        startOtpResendTimer()

        binding.tvPhoneNo.setOnClickListener {
            editPhoneNumberAndNavigateBackToAuthScreen()
        }

        binding.ivEditPhoneNo.setOnClickListener {
            editPhoneNumberAndNavigateBackToAuthScreen()
        }

        binding.tvResend.setOnClickListener {
            resendOtpToPhoneNumber()
        }

        binding.btnVerifyOtp.setOnClickListener {
            val otp = binding.etOtpPin.editableText.toString()
            val verificationId = args.verificationId
            if (otp != "") {
                val credentials = PhoneAuthProvider.getCredential(
                    verificationId,
                    otp
                )
                binding.progressBar.visibility = View.VISIBLE
                // Depending on login or registration, call the appropriate function
                lifecycleScope.launch(Dispatchers.IO) {
                    if (args.loginOrRegister == "Login") {
                        siginWithPhoneNumber(credentials)
                    } else if (args.loginOrRegister == "Register") {
                        sigUpWithPhoneNumber(credentials)
                    }

                }
            } else {
                Toast.makeText(requireContext(), "Please Enter Otp!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to set up the hidden phone number view
    private fun setupPhoneNumberTextView() {
        phoneNumber = args.phoneNumber
        val hiddenPhoneNumberText =
            "+91${phoneNumber.get(3)}${phoneNumber.get(4)}******${phoneNumber.get(11)}${
                phoneNumber.get(12)
            }"
        binding.tvPhoneNo.text = hiddenPhoneNumberText
    }

    // Function to navigate back to the authentication screen for editing phone number
    private fun editPhoneNumberAndNavigateBackToAuthScreen() {
        findNavController().navigate(R.id.action_otpFragment_to_authFragment)
    }

    // Function to start the OTP resend timer
    private fun startOtpResendTimer() {
        currentCounterTimeInMilliSeconds = COUNTDOWNTIMEINMINUTE.toLong() * 60000L
        countDownTimer = object : CountDownTimer(currentCounterTimeInMilliSeconds, 1000) {
            override fun onTick(p0: Long) {
                currentCounterTimeInMilliSeconds = p0
                updateTimerUri()
            }

            override fun onFinish() {
                binding.tvTimer.visibility = View.GONE
                binding.tvResend.text = "Resend OTP"
                binding.tvResend.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.blueResendTextColor
                    )
                )
                isTimerRunning = false
            }

        }
        countDownTimer.start()

        isTimerRunning = true
    }

    // Function to update the OTP resend timer UI
    private fun updateTimerUri() {
        val minute = (currentCounterTimeInMilliSeconds / 1000) / 60
        val seconds = (currentCounterTimeInMilliSeconds / 1000) % 60

        val formattedSeconds = String.format("%02d", seconds)
        binding.tvTimer.text = "$minute:$formattedSeconds"
    }

    // Function to resend OTP to the phone number
    private fun resendOtpToPhoneNumber() {
        if (isTimerRunning == true) {
            Log.d(TAG, "Timer is Running")
        } else {
            val resendToken = args.resendToken.resendToken
            val options = PhoneAuthOptions.newBuilder(Firebase.auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(callbacks)
                .setForceResendingToken(resendToken!!)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)

            binding.tvResend.text = "Resend OTP in: "
            binding.tvResend.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
            binding.tvTimer.visibility = View.VISIBLE
            startOtpResendTimer()
        }
    }

    // Function to sign in with the provided phone number and OTP (for login)
    suspend private fun siginWithPhoneNumber(credentials: PhoneAuthCredential) {
        viewModel?.signInWithPhoneNumber(credentials)
        delay(3000)

        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
        }

        viewModel?.loginFlow?.catch {

            Toast.makeText(context, it?.message.toString(), Toast.LENGTH_SHORT).show()
            Log.d(FIRESTOREDATASTATUS, it?.message.toString())

        }?.collect { it ->
            when (it) {
                is NetworkResult.Error -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, it?.message.toString(), Toast.LENGTH_SHORT).show()
                        Log.d(FIRESTOREDATASTATUS, it?.message.toString())
                    }
                }

                is NetworkResult.Loading -> {
                    Log.d(TAG, "Loading")
                }

                is NetworkResult.Success -> {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        lifecycleScope.launch(Dispatchers.IO) {
                            checkIfUserExist("Login")
                        }
                    }

                }

                else -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, it?.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
    }

    // Function to sign up with the provided phone number and OTP (for registration)
    suspend private fun sigUpWithPhoneNumber(credentials: PhoneAuthCredential) {
        viewModel?.signInWithPhoneNumber(credentials)
        delay(3000)

        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
        }

        viewModel?.loginFlow?.catch {

            Toast.makeText(context, it?.message.toString(), Toast.LENGTH_SHORT).show()
            Log.d(FIRESTOREDATASTATUS, it?.message.toString())

        }?.collect { it ->
            when (it) {
                is NetworkResult.Error -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, it?.message.toString(), Toast.LENGTH_SHORT).show()
                        Log.d(FIRESTOREDATASTATUS, it?.message.toString())
                    }
                }

                is NetworkResult.Loading -> {
                    Log.d(TAG, "Loading")
                }

                is NetworkResult.Success -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        checkIfUserExist("Register")
                    }
                }

                else -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, it?.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
    }

    // Function to check if the user already exists (for registration)
    private suspend fun checkIfUserExist(loginOrRegister: String) {
        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.VISIBLE
        }
        viewModel.checkIfUserAlreadyExist()
        Log.d(FIRESTOREDATASTATUS, "Entering")
        viewModel?.userExistFlow?.collect {
            when (it) {
                is NetworkResult.Error -> {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, it?.message.toString(), Toast.LENGTH_SHORT).show()
                        Log.d(Constants.FIRESTOREDATASTATUS, it?.message.toString())
                    }
                }

                is NetworkResult.Loading -> {
                    withContext(Dispatchers.Main) {
                        Log.d(FIRESTOREDATASTATUS, "Loading")
                    }
                }

                is NetworkResult.Success -> {
                    Log.d(FIRESTOREDATASTATUS, "Success")
                    if (it.data == true) {
                        if (loginOrRegister == "Register") {
                            withContext(Dispatchers.Main) {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    context,
                                    "User with this phone number already exists.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                viewModel.signout()
                            }
                        } else {
                            withContext(Dispatchers.Main){
                                findNavController().navigate(R.id.action_otpFragment_to_homeFragment)
                            }
                            countDownTimer.cancel()
                        }

                    } else {
                        if (loginOrRegister == "Register") {
                            lifecycleScope.launch(Dispatchers.IO) {
                                withContext(Dispatchers.Main) {
                                    binding.progressBar.visibility = View.GONE
                                    findNavController().navigate(R.id.action_otpFragment_to_homeFragment)

                                    countDownTimer.cancel()

                                    if (firebaseAuth.uid != null) {
                                        Log.d(FIRESTOREDATASTATUS, "UId is not null")
                                        addDoctorDataToFirestore(args.doctorData)
                                    }
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    requireContext(),
                                    "Please Register Yourself First!",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                            viewModel.deleteUser()
                        }

                    }

                }

                else -> {
                    Log.d(FIRESTOREDATASTATUS, "ELSE BLOCK")
                }
            }
        }
    }

    // Function to add doctor data to Firestore (for registration)
    private suspend fun addDoctorDataToFirestore(doctorData: DoctorData) {
        viewModel?.addDoctorDataToFirestore(doctorData)

        viewModel?.doctorDataFlow?.collect { it ->
            when (it) {
                is NetworkResult.Error -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                }

                is NetworkResult.Loading -> {
                    Log.d(Constants.TAG, "LOADING")
                }

                is NetworkResult.Success -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), it.data.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                else -> {
                    Log.d(TAG, "Adding data to firestore")
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}