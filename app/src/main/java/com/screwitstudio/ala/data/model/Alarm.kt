package com.screwitstudio.ala.data.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Alarm")
data class Alarm(
    @PrimaryKey(autoGenerate = false)
    var id: Long = generateUniqueLongId(), // Primary key, auto-generated
    var title:String? = null,
    var timeMillis: Long, // Time in milliseconds
    var selectedDays: List<String>, // List of selected days (e.g., ["Monday", "Saturday", "Sunday"])
    var isEnabled: Boolean, // Alarm state (true for enabled, false for disabled)
    var pendingIntentIds: List<Int> // List of unique identifiers for pending intents
)

fun generateUniqueLongId(): Long {
    val timestamp = System.currentTimeMillis()
    val random = (0..9999).random() // Generate a random number between 0 and 9999
    // Combine the timestamp and random number to create a unique Long ID
    return timestamp * 10000L + random
}

