package com.screwitstudio.ala.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.screwitstudio.ala.data.model.Alarm

@Database(entities = [Alarm::class], version = 3)
@TypeConverters(ConvertersString::class,ConvertersInt::class)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object{
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the new 'title' column to the 'alarms' table
                database.execSQL("ALTER TABLE Alarm ADD COLUMN title TEXT")
            }
        }
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {

            }
        }
    }
}