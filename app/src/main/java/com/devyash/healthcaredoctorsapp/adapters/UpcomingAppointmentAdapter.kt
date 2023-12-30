package com.devyash.healthcaredoctorsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.devyash.healthcaredoctorsapp.R
import com.devyash.healthcaredoctorsapp.databinding.AppointmentItemLayoutBinding
import com.devyash.healthcaredoctorsapp.models.DetailedDoctorAppointment
import com.devyash.healthcaredoctorsapp.utils.DoctorDiffUtil
import java.text.SimpleDateFormat
import java.util.Locale

class UpcomingAppointmentAdapter:RecyclerView.Adapter<UpcomingAppointmentAdapter.UpcomingAppointmentsViewHolder>() {

    private val asyncListDiffer = AsyncListDiffer<DetailedDoctorAppointment>(this,DoctorDiffUtil())
    inner class UpcomingAppointmentsViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val binding = AppointmentItemLayoutBinding.bind(itemView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UpcomingAppointmentsViewHolder {
        val viewHolder = UpcomingAppointmentsViewHolder(
            LayoutInflater.from(parent.context).inflate(
            R.layout.appointment_item_layout,parent,false))

        return viewHolder
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: UpcomingAppointmentsViewHolder, position: Int) {
       val doctorAppointment  = asyncListDiffer.currentList[position]

        holder.binding.tvAppointmentStatus.text = doctorAppointment.status
        holder.binding.tvAppointmentConsultText.text = doctorAppointment.typeOfConsultation

        if(doctorAppointment.typeOfConsultation == "Clinic Visit"){
            holder.binding.cvVideoCall.visibility = View.GONE
            holder.binding.cvChat.visibility = View.VISIBLE
        }

        val timeStampDate = doctorAppointment.dateTime.toDate()
        val simpleDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val formattedDate = simpleDateFormat.format(timeStampDate)

        val timeStampTime = doctorAppointment.dateTime.toDate().time
        val simpleDateFormatTime = SimpleDateFormat("h a", Locale.getDefault())
        val formattedTime = simpleDateFormatTime.format(timeStampTime)

        holder.binding.tvAppointmentDate.text = formattedDate.toString()
        holder.binding.tvAppointmentTime.text = formattedTime.toString()
    }

    fun setData(newList:List<DetailedDoctorAppointment>){
        asyncListDiffer.submitList(newList)
    }
}