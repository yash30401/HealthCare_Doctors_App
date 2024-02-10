package com.devyash.healthcaredoctorsapp.ui.fragments

import android.Manifest
import android.content.Context
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
import com.devyash.healthcaredoctorsapp.VideoCalling.RTCClient
import com.devyash.healthcaredoctorsapp.VideoCalling.models.IceCandidateModel.IceCandidateModel
import com.devyash.healthcaredoctorsapp.VideoCalling.models.TYPE
import com.devyash.healthcaredoctorsapp.VideoCalling.repository.SocketRepository
import com.devyash.healthcaredoctorsapp.VideoCalling.utils.NewMessageInterface
import com.devyash.healthcaredoctorsapp.VideoCalling.utils.PeerConnectionObserver
import com.devyash.healthcaredoctorsapp.VideoCalling.utils.RtcAudioManager
import com.devyash.healthcaredoctorsapp.adapters.SlotAdapter
import com.devyash.healthcaredoctorsapp.adapters.UpcomingAppointmentAdapter
import com.devyash.healthcaredoctorsapp.databinding.FragmentHomeBinding
import com.devyash.healthcaredoctorsapp.models.MessageModel.MessageModel
import com.devyash.healthcaredoctorsapp.models.SlotList
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.others.ChatClickListner
import com.devyash.healthcaredoctorsapp.others.Constants
import com.devyash.healthcaredoctorsapp.others.Constants.DELETESLOT
import com.devyash.healthcaredoctorsapp.others.Constants.FETCHAPPOINTMENTS
import com.devyash.healthcaredoctorsapp.others.Constants.GETTINGSLOTSFROMFIREBASE
import com.devyash.healthcaredoctorsapp.others.Constants.HEADERLAYOUTTAG
import com.devyash.healthcaredoctorsapp.others.Constants.MAINFRAGMENTTAG
import com.devyash.healthcaredoctorsapp.others.Constants.SLOTTESTING
import com.devyash.healthcaredoctorsapp.utils.BottomNavigationVisibilityListener
import com.devyash.healthcaredoctorsapp.viewmodels.AppointmentViewModel
import com.devyash.healthcaredoctorsapp.viewmodels.AuthViewModel
import com.devyash.healthcaredoctorsapp.viewmodels.SlotViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), AddDateTimeClickListener, ChatClickListner,
    UpcomingAppointmentAdapter.VideoCallClickListner, NewMessageInterface {

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

    lateinit var socketRepository: SocketRepository

    lateinit var uid: String
    lateinit var targetUID: String

    private var rtcClient: RTCClient? = null
    private val gson = Gson()
    private var isMute = false
    private var isCameraPause = false
    private val rtcAudioManager by lazy { RtcAudioManager.create(requireContext()) }
    private var isSpeakerMode = true

    private var bottomNavigationVisibilityListener: BottomNavigationVisibilityListener? = null
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

        uid = firebaseAuth.uid.toString()

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

        slotAdapter = SlotAdapter(ContextCompat.getDrawable(requireContext(), R.drawable.add)!!)
        setupUpcomingAppointmentRecylerView()
        setupSlotRecylerView()

        init()

        getPermissionsForVideoCall()
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
        upcomingAppointmentAdapter = UpcomingAppointmentAdapter(this, this)
        binding.rvUpcomingAppointments.apply {
            adapter = upcomingAppointmentAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
        fetchUpcomingAppointments()
    }

    private fun fetchUpcomingAppointments() {
        lifecycleScope.launch {
            appointmentViewModel.upcomingAppointments.collect {
                when (it) {
                    is NetworkResult.Error -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Problem in fetching Upcoming Appointments",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.d(FETCHAPPOINTMENTS, "Error:- " + it.message.toString())
                        }
                    }

                    is NetworkResult.Loading -> {
                        Log.d(FETCHAPPOINTMENTS, "Loading:- " + it.message.toString())
                    }

                    is NetworkResult.Success -> {
                        withContext(Dispatchers.Main) {
                            upcomingAppointmentAdapter.setData(it.data?.toList()!!)
                            Log.d(FETCHAPPOINTMENTS, "Success")
                        }
                    }

                    else -> {

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
                        withContext(Dispatchers.Main) {
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
                                datePickerFragment.show(
                                    requireActivity().supportFragmentManager,
                                    "datePicker"
                                )
                            }

                            slotAdapter.deleteClickListner = { view, position ->
                                val dialog = MaterialAlertDialogBuilder(requireContext())
                                    .setCancelable(true)
                                    .setTitle("Delete Slot")
                                    .setMessage("Do you want to delete this slot?")
                                    .setNegativeButton(
                                        "Cancel",
                                        DialogInterface.OnClickListener { dialogInterface, i ->
                                            dialogInterface.dismiss()
                                        })
                                    .setPositiveButton(
                                        "Delete",
                                        DialogInterface.OnClickListener { dialogInterface, i ->
                                            deleteSlotOnFirebase(position, it.data.toList())
                                        }).show()
                            }

                            binding.rvSlot.apply {
                                adapter = slotAdapter
                                layoutManager =
                                    LinearLayoutManager(
                                        requireContext(),
                                        LinearLayoutManager.HORIZONTAL,
                                        false
                                    )
                            }

                        }
                        withContext(Dispatchers.Main) {
                            binding.slotProgressBar.visibility = View.INVISIBLE
                        }
                    }

                    else -> {
                        Log.d(GETTINGSLOTSFROMFIREBASE, "Else Block:- ${it?.message.toString()}")
                        withContext(Dispatchers.Main) {
                            binding.slotProgressBar.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }

    }

    private fun deleteSlotOnFirebase(position: Int, slotList: List<Long>) {
        lifecycleScope.launch {
            slotViewModel.deleteSlot(slotList.get(position))

            slotViewModel.deleteSlot.collect {
                when (it) {
                    is NetworkResult.Error -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Problem in Deleting Slot",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.d(DELETESLOT, "Error:- " + it.message.toString())
                        }
                    }

                    is NetworkResult.Loading -> {
                        Log.d(DELETESLOT, "Loading:- " + it.message.toString())
                    }

                    is NetworkResult.Success -> {
                        withContext(Dispatchers.Main) {
                            slotAdapter.deleteSlot(position)
                            Log.d(DELETESLOT, "Success")
                            Toast.makeText(requireContext(), "Slot Deleted", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                    else -> {

                    }
                }
            }
        }
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
                            Log.d(DELETESLOT, "Error:- " + it.message.toString())
                        }
                    }

                    is NetworkResult.Loading -> {
                        Log.d(SLOTTESTING, "Loading Block:- ${it?.message.toString()}")
                    }

                    is NetworkResult.Success -> {
                        Log.d(SLOTTESTING, "Success Block Data:- ${it?.data.toString()}")
                        withContext(Dispatchers.Main) {
                            slotAdapter.addItemToTheList(time)
                        }
                    }

                    else -> {
                        Log.d(SLOTTESTING, "Else Block:- ${it?.message.toString()}")
                    }
                }
            }
        }
    }

    override fun onClick(userId: String) {
        val action = HomeFragmentDirections.actionHomeFragmentToChattingFragment(userId)
        findNavController().navigate(action)
    }

    private fun init() {
        socketRepository = SocketRepository(this)
        uid?.let { socketRepository?.initSocket(it) }
        rtcClient = RTCClient(
            activity?.application!!,
            uid!!,
            socketRepository!!,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    rtcClient?.addIceCandidate(p0)
                    val candidate = hashMapOf(
                        "sdpMid" to p0?.sdpMid,
                        "sdpMLineIndex" to p0?.sdpMLineIndex,
                        "sdpCandidate" to p0?.sdp
                    )

                    socketRepository?.sendMessageToSocket(
                        MessageModel(TYPE.ICE_CANDIDATE, uid, targetUID, candidate)
                    )
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    p0?.videoTracks?.get(0)?.addSink(binding?.remoteView)
                    Log.d(Constants.VIDEOCALLINGWEBRTC, "onAddStream: $p0")
                }
            })

//        rtcClient?.initializeSurfaceView(binding!!.localView)
//        rtcClient?.startLocalVideo(binding!!.localView)
        rtcAudioManager.setDefaultAudioDevice(RtcAudioManager.AudioDevice.SPEAKER_PHONE)


        binding?.switchCameraButton?.setOnClickListener {
            rtcClient?.switchCamera()
        }

        binding?.micButton?.setOnClickListener {
            if (isMute) {
                isMute = false
                binding!!.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24)
            } else {
                isMute = true
                binding!!.micButton.setImageResource(R.drawable.ic_baseline_mic_24)
            }
            rtcClient?.toggleAudio(isMute)
        }

        binding?.videoButton?.setOnClickListener {
            if (isCameraPause) {
                isCameraPause = false
                binding!!.videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24)
            } else {
                isCameraPause = true
                binding!!.videoButton.setImageResource(R.drawable.ic_baseline_videocam_24)
            }
            rtcClient?.toggleCamera(isCameraPause)
        }

        binding?.audioOutputButton?.setOnClickListener {
            if (isSpeakerMode) {
                isSpeakerMode = false
                binding!!.audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24)
                rtcAudioManager.setDefaultAudioDevice(RtcAudioManager.AudioDevice.EARPIECE)
            } else {
                isSpeakerMode = true
                binding!!.audioOutputButton.setImageResource(R.drawable.ic_baseline_speaker_up_24)
                rtcAudioManager.setDefaultAudioDevice(RtcAudioManager.AudioDevice.SPEAKER_PHONE)

            }

        }

        binding?.endCallButton?.setOnClickListener {
            setCallLayoutGone()
            setIncomingCallLayoutGone()
            rtcClient?.endCall()
            bottomNavigationVisibilityListener?.setBottomNavigationVisibility(true)
            val message = MessageModel(TYPE.CALL_ENDED, uid, targetUID, null)
            socketRepository?.sendMessageToSocket(message)
        }

    }

    private fun getPermissionsForVideoCall() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            ).request { allGranted, _, _ ->
                if (allGranted) {


                } else {
                    Toast.makeText(requireContext(), "you should accept all permissions", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    override fun onNewMessage(message: MessageModel) {
        when(message.type){
            TYPE.CALL_RESPONSE->{
                if (message.data == "user is not online"){
                    //user is not reachable
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(),"user is not reachable", Toast.LENGTH_LONG).show()

                        }
                    }
                }else{
                    //we are ready for call, we started a call
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main){
                            setCallLayoutVisible()
                            binding?.apply {
                                rtcClient?.initializeSurfaceView(binding.localView)
                                rtcClient?.initializeSurfaceView(binding.remoteView)
                                rtcClient?.startLocalVideo(binding.localView)
                                rtcClient?.call(targetUID)
                            }

                        }
                    }
                }
            }
            TYPE.ANSWER_RECIEVED ->{

                val session = SessionDescription(
                    SessionDescription.Type.ANSWER,
                    message.data.toString()
                )
                rtcClient?.onRemoteSessionReceived(session)
                lifecycleScope.launch {
                    withContext(Dispatchers.Main){
                        binding?.remoteViewLoading?.visibility = View.GONE
                    }
                }
            }
            TYPE.OFFER_RECIEVED ->{
                Log.d("OFEERWEBRTC","Recived")

                lifecycleScope.launch {
                    withContext(Dispatchers.Main){
                        setIncomingCallLayoutVisible()
                        binding?.incomingNameTV?.text = "${message.name.toString()} is calling you"
                        binding?.acceptButton?.setOnClickListener {
                            setIncomingCallLayoutGone()
                            setCallLayoutVisible()
                            bottomNavigationVisibilityListener?.setBottomNavigationVisibility(false)
                            binding?.apply {
                                rtcClient?.initializeSurfaceView(localView)
                                rtcClient?.initializeSurfaceView(binding.remoteView)
                                rtcClient?.startLocalVideo(localView)
                            }
                            val session = SessionDescription(
                                SessionDescription.Type.OFFER,
                                message.data.toString()
                            )
                            Log.d("OFEERWEBRTC","UID:- ${message.name}")
                            rtcClient?.onRemoteSessionReceived(session)
                            rtcClient?.answer(message.name!!)
                            targetUID = message.name!!
                            binding!!.remoteViewLoading.visibility = View.GONE

                        }
                        binding?.rejectButton?.setOnClickListener {
                            setIncomingCallLayoutGone()
                            bottomNavigationVisibilityListener?.setBottomNavigationVisibility(false)
                        }

                    }
                }

            }


            TYPE.ICE_CANDIDATE->{
                try {
                    val receivingCandidate = gson.fromJson(gson.toJson(message.data),
                        IceCandidateModel::class.java)
                    rtcClient?.addIceCandidate(
                        IceCandidate(receivingCandidate.sdpMid,
                            Math.toIntExact(receivingCandidate.sdpMLineIndex.toLong()),receivingCandidate.sdpCandidate)
                    )
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            TYPE.CALL_ENDED -> {
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "The call has ended", Toast.LENGTH_LONG).show()
                        bottomNavigationVisibilityListener?.setBottomNavigationVisibility(true)
                        rtcClient?.endCall()
                        binding.callLayout.visibility = View.GONE
                        setIncomingCallLayoutGone()
                    }
                }
            }

            else -> {}
        }
    }


    private fun setIncomingCallLayoutGone() {
        binding?.incomingCallLayout?.visibility = View.GONE
    }

    private fun setIncomingCallLayoutVisible() {
        binding?.incomingCallLayout?.visibility = View.VISIBLE
    }

    private fun setCallLayoutGone() {
        binding?.callLayout?.visibility = View.GONE
    }

    private fun setCallLayoutVisible() {
        binding?.callLayout?.visibility = View.VISIBLE
    }


    override fun onclick(userUId: String) {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            ).request { allGranted, _, _ ->
                if (allGranted) {
                    targetUID = userUId
                    socketRepository?.sendMessageToSocket(
                        MessageModel(
                            TYPE.START_CALL, uid, targetUID, null
                        )
                    )
                    bottomNavigationVisibilityListener?.setBottomNavigationVisibility(false)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "you should accept all permissions",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationVisibilityListener) {
            bottomNavigationVisibilityListener = context
        } else {
            throw RuntimeException("$context must implement BottomNavigationVisibilityListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        bottomNavigationVisibilityListener = null
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        rtcClient?.endCall() // Close any existing WebRTC connections
        rtcClient = null
        socketRepository.closeConnection()
    }
}


