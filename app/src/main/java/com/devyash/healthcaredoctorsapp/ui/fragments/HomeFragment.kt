package com.devyash.healthcaredoctorsapp.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devyash.healthcaredoctorsapp.R
import com.devyash.healthcaredoctorsapp.adapters.SlotAdapter
import com.devyash.healthcaredoctorsapp.adapters.UpcomingAppointmentAdapter
import com.devyash.healthcaredoctorsapp.databinding.FragmentHomeBinding
import com.devyash.healthcaredoctorsapp.models.SlotList
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.others.Constants.DELETESLOT
import com.devyash.healthcaredoctorsapp.others.Constants.FETCHAPPOINTMENTS
import com.devyash.healthcaredoctorsapp.others.Constants.GETTINGSLOTSFROMFIREBASE
import com.devyash.healthcaredoctorsapp.others.Constants.HEADERLAYOUTTAG
import com.devyash.healthcaredoctorsapp.others.Constants.MAINFRAGMENTTAG
import com.devyash.healthcaredoctorsapp.others.Constants.SLOTTESTING
import com.devyash.healthcaredoctorsapp.viewmodels.AppointmentViewModel
import com.devyash.healthcaredoctorsapp.viewmodels.AuthViewModel
import com.devyash.healthcaredoctorsapp.viewmodels.SlotViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), AddDateTimeClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private val viewModel by viewModels<AuthViewModel>()
    private val slotViewModel by viewModels<SlotViewModel>()
    private val appointmentViewModel by viewModels<AppointmentViewModel>()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var slotAdapter: SlotAdapter
    private lateinit var upcomingAppointmentAdapter: UpcomingAppointmentAdapter

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

        Log.d("FIRESBASEUID", firebaseAuth.uid.toString())

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

        val fragmentManager = activity?.supportFragmentManager

        setupNavigationHeader()

        slotAdapter = SlotAdapter(ContextCompat.getDrawable(requireContext(),R.drawable.add)!!)
        setupUpcomingAppointmentRecylerView()
        setupSlotRecylerView()
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

    private fun setupUpcomingAppointmentRecylerView() {
        upcomingAppointmentAdapter = UpcomingAppointmentAdapter()
        binding.rvUpcomingAppointments.apply {
            adapter = upcomingAppointmentAdapter
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        }
        fetchUpcomingAppointments()
    }

    private fun fetchUpcomingAppointments() {
        lifecycleScope.launch{
            appointmentViewModel.upcomingAppointments.collect{
                when(it){
                    is NetworkResult.Error ->{
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Problem in fetching Upcoming Appointments",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.d(FETCHAPPOINTMENTS, "Error:- "+it.message.toString())
                        }
                    }
                    is NetworkResult.Loading -> {
                        Log.d(FETCHAPPOINTMENTS, "Loading:- "+it.message.toString())
                    }
                    is NetworkResult.Success -> {
                        withContext(Dispatchers.Main) {
                            upcomingAppointmentAdapter.setData(it.data?.toList()!!)
                            Log.d(FETCHAPPOINTMENTS, "Success")
                        }
                    }
                    else ->{

                    }
                }
            }
        }
    }


    private fun setupSlotRecylerView() {
        slotViewModel.getAllSlots()

        lifecycleScope.launch(Dispatchers.IO) {
            slotViewModel.allSlotFlow.collect {
                when (it) {
                    is NetworkResult.Error -> {
                        Log.d(GETTINGSLOTSFROMFIREBASE, "Error Block:- ${it?.message.toString()}")
                        withContext(Dispatchers.Main){
                            binding.slotProgressBar.visibility = View.INVISIBLE
                        }
                    }

                    is NetworkResult.Loading -> Log.d(
                        GETTINGSLOTSFROMFIREBASE,
                        "Loading Block:- ${it?.message.toString()}"
                    )

                    is NetworkResult.Success -> {
                        Log.d(GETTINGSLOTSFROMFIREBASE, "Success Block:- ${it?.data.toString()}")
                        withContext(Dispatchers.Main) {

                            slotAdapter.showFirebaseList(it.data?.toList()!!)

                            slotAdapter.itemClickListener = { view, position ->

                                val datePickerFragment = DatePickerDialogFragment(this@HomeFragment)
                                datePickerFragment.show(requireActivity().supportFragmentManager, "datePicker")
                            }

                            slotAdapter.deleteClickListner = {view,position->
                                val dialog = MaterialAlertDialogBuilder(requireContext())
                                    .setCancelable(true)
                                    .setTitle("Delete Slot")
                                    .setMessage("Do you want to delete this slot?")
                                    .setNegativeButton("Cancel",DialogInterface.OnClickListener{dialogInterface, i ->  
                                        dialogInterface.dismiss()
                                    })
                                    .setPositiveButton("Delete",DialogInterface.OnClickListener{dialogInterface, i ->
                                        deleteSlotOnFirebase(position)
                                    }).show()
                            }

                                binding.rvSlot.apply {
                                    adapter = slotAdapter
                                    layoutManager =
                                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                                }

                        }
                        withContext(Dispatchers.Main){
                            binding.slotProgressBar.visibility = View.INVISIBLE
                        }
                    }

                    else -> {
                        Log.d(GETTINGSLOTSFROMFIREBASE, "Else Block:- ${it?.message.toString()}")
                        withContext(Dispatchers.Main){
                            binding.slotProgressBar.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }

    }

    private fun deleteSlotOnFirebase(position: Int) {
        lifecycleScope.launch {
            slotViewModel.deleteSlot(position)

            slotViewModel.deleteSlot.collect{
                when(it){
                    is NetworkResult.Error -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Problem in Deleting Slot",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.d(DELETESLOT, "Error:- "+it.message.toString())
                        }
                    }
                    is NetworkResult.Loading -> {
                        Log.d(DELETESLOT, "Loading:- "+it.message.toString())
                    }
                    is NetworkResult.Success -> {
                        withContext(Dispatchers.Main) {
                            slotAdapter.deleteSlot(position)
                            Log.d(DELETESLOT, "Success")
                            Toast.makeText(requireContext(), "Slot Deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {

                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onDateSelected() {
        val timePickerFragment = TimePickerDialogFragment(this)
        timePickerFragment.show(requireActivity().supportFragmentManager, "timePicker")
    }

    override fun onTimeSelected(time: Long) {
        Log.d("TIMECHECKING", "DATETIME:- $time")



        lifecycleScope.launch(Dispatchers.IO) {
                time?.let { SlotList(it) }
                    ?.let { slotViewModel.addSlotToFirebase(it) }

            slotViewModel.slotFlow.collect {
                when (it) {
                    is NetworkResult.Error -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                it.message.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                            Log.d(DELETESLOT, "Error:- "+it.message.toString())
                        }
                    }

                    is NetworkResult.Loading -> {
                        Log.d(SLOTTESTING, "Loading Block:- ${it?.message.toString()}")
                    }

                    is NetworkResult.Success -> {
                        Log.d(SLOTTESTING, "Success Block Data:- ${it?.data.toString()}")
                        slotAdapter.addItemToTheList(time)
                    }

                    else -> {
                        Log.d(SLOTTESTING, "Else Block:- ${it?.message.toString()}")
                    }
                }
            }
        }
    }

}