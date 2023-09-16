package com.devyash.healthcaredoctorsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.devyash.healthcaredoctorsapp.R
import com.devyash.healthcaredoctorsapp.databinding.FragmentHomeBinding
import com.devyash.healthcaredoctorsapp.others.Constants.HEADERLAYOUTTAG
import com.devyash.healthcaredoctorsapp.others.Constants.MAINFRAGMENTTAG
import com.devyash.healthcaredoctorsapp.viewmodels.AuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private val viewModel by viewModels<AuthViewModel>()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootview = inflater.inflate(R.layout.fragment_home, container, false)

        val toolbar: Toolbar = rootview.findViewById(R.id.toolbar)
        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.setDisplayShowHomeEnabled(true)

        drawerLayout = rootview.findViewById(R.id.drawerLayout)
        navigationView = rootview.findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            activity, drawerLayout, toolbar, 0, R.string.app_name
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            // Handle navigation item clicks here
            when (menuItem.itemId) {
                R.id.home -> {
                    true
                }

                R.id.aboutApp -> {
                    Log.d(MAINFRAGMENTTAG, "About App")
                    true
                }

                R.id.privacyPolicy -> {
                    Log.d(MAINFRAGMENTTAG, "Privacy Policy")
                    true
                }

                R.id.contactUs -> {
                    Log.d(MAINFRAGMENTTAG, "Contact Us")
                    true
                }

                R.id.logout -> {
                    //Logout User

                    // Slide Bottom Bottom Nav For Better UX.
                    val bottomNav =
                        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
                    val bottomAnim =
                        AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom)
                    bottomNav.startAnimation(bottomAnim)

                    viewModel.signout()
                    findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                    true
                }
                // Add more navigation items and their handling
                else -> false
            }
        }



        return rootview
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupNavigationHeader()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupNavigationHeader() {
        val headerLayout = navigationView.getHeaderView(0)
        val phoneNumber = headerLayout.findViewById<TextView>(R.id.tvPhoneNumber)

        val currentUser = firebaseAuth.currentUser
        Log.d(HEADERLAYOUTTAG, "Phone Number:- ${currentUser?.phoneNumber.toString()}")

        val hiddenPhoneNumberText =
            "+91${currentUser?.phoneNumber?.get(3)}${currentUser?.phoneNumber?.get(4)}******${
                currentUser?.phoneNumber?.get(
                    11
                )
            }${
                currentUser?.phoneNumber?.get(12)
            }"
        phoneNumber.text = hiddenPhoneNumberText
    }



    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}