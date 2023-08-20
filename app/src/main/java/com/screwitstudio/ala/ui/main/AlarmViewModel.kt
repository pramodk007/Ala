package com.screwitstudio.ala.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.screwitstudio.ala.data.model.Alarm
import com.screwitstudio.ala.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    // LiveData for all alarms
    val allAlarmsFlowData = alarmRepository.getAllAlarms().asLiveData()

    // Function to insert an alarm using a coroutine
    fun insertAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmRepository.insertAlarm(alarm)
        }
    }

    // Function to update an alarm using a coroutine
    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmRepository.updateAlarm(alarm)
        }
    }

    // Function to delete an alarm using a coroutine
    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmRepository.deleteAlarm(alarm)
        }
    }

    // Function to get a specific alarm using a coroutine
    suspend fun getAlarm(id: Long) : Alarm {
       return alarmRepository.getAlarm(id)
    }
}