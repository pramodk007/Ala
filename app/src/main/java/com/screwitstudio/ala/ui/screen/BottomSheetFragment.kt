package com.screwitstudio.ala.ui.screen

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TimePicker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.screwitstudio.ala.R
import com.screwitstudio.ala.data.model.Alarm
import java.util.Calendar

class BottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var timePicker: TimePicker
    private lateinit var chipGroupDays: ChipGroup
    private lateinit var btnCancel: Button
    private lateinit var btnSave: Button
    private var alarmID: Long = 0L

    private var alarmToEdit: Alarm? = null // Alarm to edit (null for new alarm)

    companion object {
        fun newInstance(alarmToEdit: Alarm? = null): BottomSheetFragment {
            val fragment = BottomSheetFragment()
            fragment.alarmToEdit = alarmToEdit
            return fragment
        }
    }

    // Define a mapping of short day names to full day names
    private val dayNameMap = mapOf(
        "Sun" to "Sunday",
        "Mon" to "Monday",
        "Tue" to "Tuesday",
        "Wed" to "Wednesday",
        "Thu" to "Thursday",
        "Fri" to "Friday",
        "Sat" to "Saturday"
    )

    // Define a callback interface
    interface BottomSheetListener {
        fun onBottomSheetDataSelected(
            selectedCalendar: Calendar,
            selectedDays: List<String>,
            alarmId: Long
        )
    }

    private var listener: BottomSheetListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views using findViewById
        timePicker = view.findViewById(R.id.timePicker)
        chipGroupDays = view.findViewById(R.id.chipGroupDays)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnSave = view.findViewById(R.id.btnSave)

        // Initialize UI based on whether it's a new alarm or edit
        alarmToEdit?.let { alarm ->
            alarmID = alarm.id
            //Convert timeMillis to hours and minutes
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = alarm.timeMillis
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            // Populate the UI with the existing alarm data for editing
            timePicker.hour = hour
            timePicker.minute = minute
            // Select the days in the ChipGroup
            for (i in 0 until chipGroupDays.childCount) {
                val chip = chipGroupDays.getChildAt(i) as Chip
                if (alarm.selectedDays.contains(dayNameMap[chip.text.toString()])) {
                    chip.isChecked = true
                }
            }
        }

        // Handle "Cancel" button click
        btnCancel.setOnClickListener {
            // Handle cancel action here
            dismiss() // Close the bottom sheet
        }

        // Handle "Save" button click
        btnSave.setOnClickListener {
            val selectedTime = calculateSelectedCalendar(timePicker.hour, timePicker.minute)
            val selectedDays = getSelectedChips()
            if (selectedDays.isEmpty()) {
                //No days selected, show a Snack bar to prompt the user
                showSnackbar(view, "Please select at least one day.")
            } else {
                listener?.onBottomSheetDataSelected(selectedTime, selectedDays, alarmID)
                dismiss() // Close the bottom sheet
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false // Disable dismissal by back button or tapping outside
    }

    private fun getSelectedChips(): List<String> {
        val selectedChips = mutableListOf<String>()
        for (i in 0 until chipGroupDays.childCount) {
            val chip = chipGroupDays.getChildAt(i) as Chip
            if (chip.isChecked) {
                // Use the mapping to get the full day name
                val fullDayName = dayNameMap[chip.text.toString()]
                fullDayName?.let {
                    selectedChips.add(fullDayName)
                }
            }
        }
        return selectedChips
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as BottomSheetListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement BottomSheetListener")
        }
    }

    private fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setAction("OK") {
                // Optional action to take when the "OK" button on the Snackbar is clicked
            }
            .show()
    }

    private fun calculateSelectedCalendar(selectedHour: Int, selectedMinute: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
        calendar.set(Calendar.MINUTE, selectedMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }
}
