package com.devyash.healthcaredoctorsapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.devyash.healthcaredoctorsapp.R
import com.devyash.healthcaredoctorsapp.databinding.ActivityMainBinding
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.others.Constants.FIREBASEMESSAGINTOKEN
import com.devyash.healthcaredoctorsapp.utils.BottomNavigationVisibilityListener
import com.devyash.healthcaredoctorsapp.viewmodels.FirebaseMessagingViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),BottomNavigationVisibilityListener {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private val firebaseMessagingViewModel by viewModels<FirebaseMessagingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController


        binding.bottomNav.setupWithNavController(navController)
        hideBottomNavOnAuthFragment()

        Firebase.initialize(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )


        binding.bottomNav.setOnNavigationItemSelectedListener {menuItem->
            when(menuItem.itemId){
                R.id.home -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }

                R.id.chatFragment -> {
                    navController.navigate(R.id.chatFragment)
                    true
                }

                R.id.profileFragment -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }

                else -> {
                    false
                }

            }

        }
        getFCMToken()

    }


    private fun hideBottomNavOnAuthFragment() {
        navController.addOnDestinationChangedListener { _, destination, _ ->

            if (destination.id == R.id.authFragment || destination.id == R.id.otpFragment || destination.id == R.id.loginFragment ||  destination.id == R.id.chattingFragment) {
                binding.bottomNav.visibility = View.GONE
            } else {
                binding.bottomNav.visibility = View.VISIBLE
            }
        }
    }

    override fun onBackPressed() {
        val currentDestination = navController.currentDestination
        val isLoggedIn = firebaseAuth.currentUser !=null

        Log.d("FragTesting", isLoggedIn.toString())
        Log.d("FragTesting", currentDestination.toString())

        if (isLoggedIn && currentDestination?.id == R.id.homeFragment) {
            showExitConfirmationDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun showExitConfirmationDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Exit Confirmation")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Exit") { _, _ ->
                finishAffinity()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getFCMToken() {
        firebaseMessagingViewModel.getFCMToken()

        lifecycleScope.launch{
            firebaseMessagingViewModel.token.collect{
                when(it){
                    is NetworkResult.Error -> {
                        Log.d(FIREBASEMESSAGINTOKEN,"Error Block:- ${it.message}")
                    }
                    is NetworkResult.Loading -> {
                        Log.d(FIREBASEMESSAGINTOKEN,"Loading Block:- ${it.message}")
                    }
                    is NetworkResult.Success -> {
                        Log.d(FIREBASEMESSAGINTOKEN,"Success Block:- ${it.data.toString()}")
                    }
                    else -> {}
                }
            }
        }
    }


    override fun setBottomNavigationVisibility(isVisible: Boolean) {
        binding.bottomNav.visibility = if (isVisible) View.VISIBLE else View.GONE
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}