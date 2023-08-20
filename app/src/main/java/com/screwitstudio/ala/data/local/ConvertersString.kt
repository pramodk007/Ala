package com.screwitstudio.ala.data.local

import androidx.room.TypeConverter

class ConvertersString {
    @TypeConverter
    fun fromString(value: String?): List<String>? {
        if (value == null) {
            return null
        }
        return value.split(",").map { it.trim() }
    }

    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return list?.joinToString(",") { it }
    }
}
