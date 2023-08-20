package com.screwitstudio.ala.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.screwitstudio.ala.data.model.Alarm
import kotlinx.coroutines.flow.Flow


@Dao
interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm)

    @Update
    suspend fun updateAlarm(alarm: Alarm)

    @Delete
    suspend fun deleteAlarm(alarm: Alarm)

    @Query("SELECT * FROM Alarm")
    fun getAllAlarms(): Flow<List<Alarm>>

    @Query("SELECT * FROM Alarm WHERE id = :id")
    suspend fun getAlarm(id: Long): Alarm

}