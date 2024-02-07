package com.devyash.healthcaredoctorsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.devyash.healthcaredoctorsapp.R
import com.devyash.healthcaredoctorsapp.databinding.FragmentLoginBinding
import com.devyash.healthcaredoctorsapp.models.ContactInfo
import com.devyash.healthcaredoctorsapp.models.DoctorData
import com.devyash.healthcaredoctorsapp.models.ResendTokenModelClass
import com.devyash.healthcaredoctorsapp.others.Constants
import com.devyash.healthcaredoctorsapp.others.PhoneAuthCallBackSealedClass
import com.devyash.healthcaredoctorsapp.others.PhoneNumberValidation
import com.devyash.healthcaredoctorsapp.utils.PhoneAuthCallback
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
class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!


    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var phoneAuthCallback: PhoneAuthCallback

    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private lateinit var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (firebaseAuth.currentUser != null) {
           findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }
    }

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

        callback = phoneAuthCallback.callbacks

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_authFragment)
        }

        binding.btnRequestOtp.setOnClickListener {
            val phoneNumberValidation = validateMobileNumber(binding.etMobileNo.text.toString())
            phoneNumberEventsHandle(phoneNumberValidation)
        }
    }

    private fun validateMobileNumber(number: String): PhoneNumberValidation =
        if (number.isEmpty()) PhoneNumberValidation.EMPTY else PhoneNumberValidation.SUCCESS

    private fun phoneNumberEventsHandle(phoneNumberValidation: PhoneNumberValidation) {
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
        binding.btnRequestOtp.isEnabled = false

        val phoneNumber =
            "${binding.etCountryCode.selectedCountryCodeWithPlus}${binding.etMobileNo.text.toString()}"

        GlobalScope.launch(Dispatchers.IO) {
            phoneAuthCallback.callbackFlow?.collect {
                when (it) {
                    is PhoneAuthCallBackSealedClass.FIREBASEAUTHINVALIDCREDENTIALSEXCEPTION -> {
                        Log.d(
                            Constants.AUTHVERIFICATIONTAG,
                            "onVerificationFailed: ${it.firebaseAuthInvalidCredentialsException}"
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Invalid Credentials!", Toast.LENGTH_SHORT)
                                .show()
                            binding.progressBar.visibility = View.GONE
                            binding.btnRegister.isEnabled = true
                            binding.btnRequestOtp.isEnabled = true
                        }
                    }

                    is PhoneAuthCallBackSealedClass.FIREBASEAUTHMISSINGACTIVITYFORRECAPTCHAEXCEPTION -> {
                        Log.d(
                            Constants.AUTHVERIFICATIONTAG,
                            "onVerificationFailed: ${it.firebaseAuthMissingActivityForRecaptchaException}"
                        )
                        withContext(Dispatchers.IO) {
                            Toast.makeText(context, "reCaptcha Problem", Toast.LENGTH_SHORT).show()
                            binding.progressBar.visibility = View.GONE
                            binding.btnRegister.isEnabled = true
                            binding.btnRequestOtp.isEnabled = true
                        }
                    }

                    is PhoneAuthCallBackSealedClass.FIREBASETOOMANYREQUESTSEXCEPTION -> {
                        Log.d(
                            Constants.AUTHVERIFICATIONTAG,
                            "onVerificationFailed: ${it.firebaseTooManyRequestsException}"
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Too many requests", Toast.LENGTH_SHORT).show()
                            binding.progressBar.visibility = View.GONE
                            binding.btnRegister.isEnabled = true
                            binding.btnRequestOtp.isEnabled = true
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
                                    Log.d(Constants.FACEBOOKTEST, it.message.toString())
                                }) // Resume the coroutine with the result
                            }
                        }


                        val action =
                            LoginFragmentDirections.actionLoginFragmentToOtpFragment(
                                it.verificationId.toString(),
                                phoneNumber,
                                ResendTokenModelClass(resendToken),
                                DoctorData("","", "", "", 0, 0, ContactInfo("", "", ""), 0, "","",
                                    emptyList(), emptyList(),"",""
                                ),
                                "Login"
                            )
                        withContext(Dispatchers.Main) {
                            findNavController().navigate(action)
                        }

                    }

                    is PhoneAuthCallBackSealedClass.ONVERIFICATIONCOMPLETED -> {
                        Log.d(Constants.AUTHVERIFICATIONTAG, "Verification Completed")
                    }

                    is PhoneAuthCallBackSealedClass.ONVERIFICATIONFAILED -> {
                        Log.d(
                            Constants.AUTHVERIFICATIONTAG,
                            "onVerificationFailed: ${it.firebaseException}"
                        )
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            binding.btnRegister.isEnabled = true
                            binding.btnRequestOtp.isEnabled = true
                        }
                    }

                    else -> {
                        Log.d(
                            Constants.AUTHVERIFICATIONTAG,
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}