package com.devyash.healthcaredoctorsapp.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.devyash.healthcaredoctorsapp.R

class TimePickerDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_time_layout, null)

        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)

        builder.setView(dialogView)

        return builder.create()
    }
}