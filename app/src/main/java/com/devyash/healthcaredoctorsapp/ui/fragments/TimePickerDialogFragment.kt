package com.devyash.healthcaredoctorsapp.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.findFragment
import com.devyash.healthcaredoctorsapp.R

class TimePickerDialogFragment(private val addTimeClickListner: addTimeClickListner) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_time_layout, null)

        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val btnOk  = dialogView.findViewById<Button>(R.id.btnOk)

        builder.setView(dialogView)

        btnOk.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val amPm: String = if (hour < 12) "AM" else "PM"
            val formattedHour = if (hour % 12 == 0) 12 else hour % 12
            val timeString = "$formattedHour:${String.format("%02d", minute)} $amPm"

            addTimeClickListner.onTimeSelected(timeString)
            dismiss()
        }

        return builder.create()
    }

}

interface addTimeClickListner{
    fun onTimeSelected(time:String)
}