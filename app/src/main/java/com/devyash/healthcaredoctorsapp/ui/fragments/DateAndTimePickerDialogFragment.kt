package com.devyash.healthcaredoctorsapp.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.devyash.healthcaredoctorsapp.R
import java.util.Calendar

var year:Int = 0
var month:Int= 0
var day:Int = 0

class DatePickerDialogFragment(private val addDateTimeClickListener: AddDateTimeClickListener) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_date_layout, null)

        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePicker)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        builder.setView(dialogView)

        btnOk.setOnClickListener {
             year = datePicker.year
             month = datePicker.month
             day = datePicker.dayOfMonth
            Log.d("TIMECHECKING","DayOfMonth:- ${day}")

            addDateTimeClickListener.onDateSelected()
            dismiss()
        }

        return builder.create()
    }
}


class TimePickerDialogFragment(private val addDateTimeClickListener: AddDateTimeClickListener) : DialogFragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_time_layout, null)

        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        builder.setView(dialogView)

        btnOk.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)

            addDateTimeClickListener.onTimeSelected(calendar.timeInMillis)
            dismiss()
        }

        return builder.create()
    }
}


interface AddDateTimeClickListener {
    fun onDateSelected()
    fun onTimeSelected(time: Long)
}

