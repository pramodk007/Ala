package com.screwitstudio.ala.repository

import androidx.lifecycle.MutableLiveData
import com.screwitstudio.ala.data.local.AlarmDao
import com.screwitstudio.ala.data.model.Alarm
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao
) {

    val allAlarmsLiveData = MutableLiveData<List<Alarm>>()
    var alarmMutableLiveData = MutableLiveData<Alarm>()

    fun getAllAlarms(): Flow<List<Alarm>> = alarmDao.getAllAlarms()

    suspend fun insertAlarm(alarm: Alarm) = alarmDao.insertAlarm(alarm)

    suspend fun updateAlarm(alarm: Alarm) = alarmDao.updateAlarm(alarm)

    suspend fun deleteAlarm(alarm: Alarm) = alarmDao.deleteAlarm(alarm)

    suspend fun getAlarm(id: Long) : Alarm = alarmDao.getAlarm(id)

}