package com.screwitstudio.ala.ui.main

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.screwitstudio.ala.R
import com.screwitstudio.ala.adapter.AlarmAdapter
import com.screwitstudio.ala.data.model.Alarm
import com.screwitstudio.ala.receiver.AlarmReceiver
import com.screwitstudio.ala.ui.screen.BottomSheetFragment
import com.screwitstudio.ala.utils.SwipeToDeleteCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), BottomSheetFragment.BottomSheetListener {

    private lateinit var alarmManager: AlarmManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AlarmAdapter

    // Create an instance of the ViewModel using viewModels() delegate
    private val alarmViewModel: AlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize AlarmManager using getSystemService
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = AlarmAdapter(supportFragmentManager)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        alarmViewModel.allAlarmsFlowData.observe(this) {
            adapter.run {
                this.submitList(it)
            }
        }

        val fabButton = findViewById<FloatingActionButton>(R.id.fab)

        fabButton.setOnClickListener {
            val bottomSheetFragment = BottomSheetFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        // Create an instance of SwipeToDeleteCallback
        val swipeToDeleteCallback = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val alarmToDelete = adapter.currentList[position] // Get the alarm object to delete
                deleteAlarm(alarmToDelete) // Call the method to delete the alarm from the database
            }
        }

        // Create an ItemTouchHelper with the SwipeToDeleteCallback
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onBottomSheetDataSelected(
        selectedCalendar: Calendar,
        selectedDays: List<String>,
        alarmId: Long
    ) {
        if (alarmId > 0L) {
            lifecycleScope.launch {
                val alarm = alarmViewModel.getAlarm(alarmId)
                cancelAlarmToUpdate(alarm)
                updateAlarm(selectedCalendar,selectedDays,alarmId)
            }
        } else {
            setAlarm(selectedCalendar, selectedDays)
        }

    }

    private fun setAlarm(selectedCalendar: Calendar, selectedDays: List<String>) {
        val currentTimeMillis = System.currentTimeMillis()
        val context = this

        // Create an intent to trigger the alarm (customize based on your needs)
        val intent = Intent(context, AlarmReceiver::class.java)

        // Initialize an empty list to store request codes (pendingIntentIds)
        val pendingIntentIds = mutableListOf<Int>()

        for (day in selectedDays) {
            val selectedDayOfWeek = getDayOfWeek(day)
            val calendar = Calendar.getInstance()

            // Calculate the days until the selected day of the week
            var daysUntilSelectedDay =
                (selectedDayOfWeek - calendar.get(Calendar.DAY_OF_WEEK) + 7) % 7

            // Check if the selected day is today and the alarm time is in the past
            if (daysUntilSelectedDay == 0 && selectedCalendar.timeInMillis <= currentTimeMillis) {
                // Move to the next week
                daysUntilSelectedDay = 7
            }

            // Calculate the alarm time
            calendar.timeInMillis = currentTimeMillis + (daysUntilSelectedDay * 24 * 60 * 60 * 1000) // Add days to current time
            calendar.set(Calendar.HOUR_OF_DAY, selectedCalendar.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, selectedCalendar.get(Calendar.MINUTE))
            calendar.set(Calendar.SECOND, 0)

            // Generate a unique request code
            val requestCode = generateUniqueRequestCode(day)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Print debugging information
            Log.d("AlarmDebug", "NEW ALARM : Day: $day, Request Code: $requestCode, Alarm Time: ${calendar.time}")

            // Add the request code to the list (pendingIntentIds)
            pendingIntentIds.add(requestCode)

            // Schedule the alarm for the calculated time on the selected day
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

        }

        // Create a single Alarm entity with the pendingIntentIds and other properties
        val alarmEntity = Alarm(
            timeMillis = selectedCalendar.timeInMillis,
            title = "New Alarm",
            selectedDays = selectedDays,
            isEnabled = true, // Set to true since the alarm is enabled
            pendingIntentIds = pendingIntentIds // Assign the list of request codes
        )

        // Show a Toast message indicating the alarm time and days
        val toastMessage = buildToastMessage(selectedCalendar, alarmEntity)
        Toast.makeText(context, "SET ALARM:$toastMessage", Toast.LENGTH_LONG).show()

        // Save the single Alarm entity to your database or perform any necessary actions
        alarmViewModel.insertAlarm(alarmEntity)
    }

    private fun updateAlarm(selectedCalendar: Calendar, selectedDays: List<String>, alarmId: Long) {
        val currentTimeMillis = System.currentTimeMillis()
        val context = this

        // Create an intent to trigger the alarm (customize based on your needs)
        val intent = Intent(context, AlarmReceiver::class.java)

        // Initialize an empty list to store request codes (pendingIntentIds)
        val pendingIntentIds = mutableListOf<Int>()

        for (day in selectedDays) {
            val selectedDayOfWeek = getDayOfWeek(day)
            val calendar = Calendar.getInstance()

            // Calculate the days until the selected day of the week
            var daysUntilSelectedDay =
                (selectedDayOfWeek - calendar.get(Calendar.DAY_OF_WEEK) + 7) % 7

            // Check if the selected day is today and the alarm time is in the past
            if (daysUntilSelectedDay == 0 && selectedCalendar.timeInMillis <= currentTimeMillis) {
                // Move to the next week
                daysUntilSelectedDay = 7
            }

            // Calculate the alarm time
            calendar.timeInMillis = currentTimeMillis + (daysUntilSelectedDay * 24 * 60 * 60 * 1000) // Add days to current time
            calendar.set(Calendar.HOUR_OF_DAY, selectedCalendar.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, selectedCalendar.get(Calendar.MINUTE))
            calendar.set(Calendar.SECOND, 0)

            // Generate a unique request code
            val requestCode = generateUniqueRequestCode(day)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Print debugging information
            Log.d("AlarmDebug", "UPDATED ALARM : Day: $day, Request Code: $requestCode, Alarm Time: ${calendar.time}")

            // Add the request code to the list (pendingIntentIds)
            pendingIntentIds.add(requestCode)

            // Schedule the alarm for the calculated time on the selected day
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        // Create an Alarm entity with the pendingIntentIds and other properties
        val updatedAlarmEntity = Alarm(
            id = alarmId, // Set the ID of the existing alarm
            timeMillis = selectedCalendar.timeInMillis, // Use the selectedCalendar time
            title = "Updated Alarm", // Customize the title as needed
            selectedDays = selectedDays,
            isEnabled = true, // Set to true since the alarm is enabled
            pendingIntentIds = pendingIntentIds // Assign the list of request codes
        )

        // Show a Toast message indicating the updated alarm time and days
        val toastMessage = buildToastMessage(selectedCalendar, updatedAlarmEntity)
        Toast.makeText(context, "UPDATED ALARM: $toastMessage", Toast.LENGTH_LONG).show()

        // Update the Alarm entity in your database or perform any necessary actions
        alarmViewModel.updateAlarm(updatedAlarmEntity)
    }

    fun setAlarmForSavedData(alarmEntity: Alarm) {
        val context = this

        // Create an intent to trigger the alarm (customize based on your needs)
        val intent = Intent(context, AlarmReceiver::class.java)

        // Initialize an empty list to store request codes (pendingIntentIds)
        val pendingIntentIds = mutableListOf<Int>()

        // Get the current time in milliseconds
        val currentTimeMillis = System.currentTimeMillis()

        // Initialize the base calendar instance using the alarmEntity's timeMillis
        val baseCalendar = Calendar.getInstance()
        baseCalendar.timeInMillis = alarmEntity.timeMillis

        for (day in alarmEntity.selectedDays) {
            val selectedDayOfWeek = getDayOfWeek(day)

            // Create a new calendar instance for each calculation
            val calendar = Calendar.getInstance()

            // Calculate the days until the selected day of the week
            var daysUntilSelectedDay =
                (selectedDayOfWeek - baseCalendar.get(Calendar.DAY_OF_WEEK) + 7) % 7

            // Calculate the alarm time
            calendar.timeInMillis = baseCalendar.timeInMillis
            calendar.add(Calendar.DAY_OF_WEEK, daysUntilSelectedDay)
            calendar.set(Calendar.HOUR_OF_DAY, baseCalendar.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, baseCalendar.get(Calendar.MINUTE))
            calendar.set(Calendar.SECOND, 0)

            // Check if the alarm time is in the past
            if (calendar.timeInMillis <= currentTimeMillis) {
                // Move to the next week
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }

            // Generate a unique request code
            val requestCode = generateUniqueRequestCode(day)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Print debugging information
            Log.d("AlarmDebug", "ON/OFF ALARM : Day: $day, Request Code: $requestCode, Alarm Time: ${calendar.time}")

            // Add the request code to the list (pendingIntentIds)
            pendingIntentIds.add(requestCode)

            // Schedule the alarm for the calculated time on the selected day
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        // Show a Toast message indicating the alarm time and days outside the loop
        val toastMessage = buildToastMessage(baseCalendar, alarmEntity)
        Toast.makeText(context, "UPDATE ALARM:$toastMessage", Toast.LENGTH_LONG).show()

        // Update the Alarm entity with the pendingIntentIds and set isEnabled to true
        alarmEntity.timeMillis = baseCalendar.timeInMillis
        alarmEntity.title = alarmEntity.title
        alarmEntity.selectedDays = alarmEntity.selectedDays
        alarmEntity.pendingIntentIds = pendingIntentIds
        alarmEntity.isEnabled = true

        // Update the Alarm entity in your database or perform any necessary actions
        alarmViewModel.updateAlarm(alarmEntity)
    }

    private fun buildToastMessage(calendar: Calendar, alarmEntity: Alarm): String {
        val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
        val selectedDays = alarmEntity.selectedDays.joinToString(", ")
        return "Alarm set for $formattedTime on $selectedDays"
    }

    fun cancelAlarm(alarm: Alarm) {
        val context = this

        // Create an intent that matches the one used to set the alarm
        val intent = Intent(context, AlarmReceiver::class.java)

        // Iterate through the pendingIntentIds (request codes) and cancel the associated alarms
        for (requestCode in alarm.pendingIntentIds) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Cancel the alarm using the PendingIntent
            alarmManager.cancel(pendingIntent)
        }
        alarm.isEnabled = false // Set it to false since you're canceling the alarm
        alarmViewModel.updateAlarm(alarm)
    }

    private fun cancelAlarmToUpdate(alarm: Alarm) {
        val context = this

        // Create an intent that matches the one used to set the alarm
        val intent = Intent(context, AlarmReceiver::class.java)

        // Iterate through the pendingIntentIds (request codes) and cancel the associated alarms
        for (requestCode in alarm.pendingIntentIds) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Cancel the alarm using the PendingIntent
            alarmManager.cancel(pendingIntent)
        }
    }

    // Function to generate a unique request code based on the day
    private fun generateUniqueRequestCode(day: String): Int {
        val timestamp = System.currentTimeMillis()
        val random = (0..9999).random() // Generate a random number between 0 and 9999
        // Combine the timestamp and random number to create a unique request code
        return "$day$timestamp$random".hashCode()
    }

    private fun getDayOfWeek(day: String): Int {
        return when (day.lowercase(Locale.ROOT)) {
            "sunday" -> Calendar.SUNDAY
            "monday" -> Calendar.MONDAY
            "tuesday" -> Calendar.TUESDAY
            "wednesday" -> Calendar.WEDNESDAY
            "thursday" -> Calendar.THURSDAY
            "friday" -> Calendar.FRIDAY
            "saturday" -> Calendar.SATURDAY
            else -> throw IllegalArgumentException("Invalid day: $day")
        }
    }

    private fun deleteAlarm(alarm: Alarm) {
        cancelAlarmToUpdate(alarm)
        // Delete the alarm from the database using your ViewModel
        alarmViewModel.deleteAlarm(alarm)

        // Show a Toast message indicating that the alarm has been deleted
        Toast.makeText(this, "Deleted alarm: ${alarm.id}", Toast.LENGTH_SHORT).show()
    }
}