package com.screwitstudio.ala.data.local

import androidx.room.TypeConverter

class ConvertersInt {
    @TypeConverter
    fun fromString(value: String?): List<Int>? {
        if (value == null) {
            return null
        }
        return value.split(",").map { it.toInt() }
    }

    @TypeConverter
    fun fromList(list: List<Int>?): String? {
        return list?.joinToString(",") { it.toString() }
    }
}
