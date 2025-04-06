package com.example.appscheduler.ui.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.appscheduler.data.models.Schedule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _scheduleList = MutableStateFlow<List<Schedule>>(emptyList())
    private val _scheduleItem = MutableStateFlow(Schedule("", LocalDateTime.now()))
    private val _showDialog = MutableStateFlow(false)

    val scheduleList = _scheduleList.asStateFlow()
    val scheduleItem = _scheduleItem.asStateFlow()
    val showDialog = _showDialog.asStateFlow()

    private val scheduleListKey = "alarm_list"
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(scheduleListKey, Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        loadScheduleListFromPreferences()
    }

    private fun loadScheduleListFromPreferences() {
        val json = sharedPreferences.getString(scheduleListKey, null)
        if (json != null) {
            val type = object : TypeToken<List<Schedule>>() {}.type
            val scheduleList = gson.fromJson<List<Schedule>>(json, type)
            _scheduleList.value = scheduleList
        }
    }
    fun saveSchedule(schedule: Schedule) {
        _scheduleItem.value = schedule
        // Add the new schedule to the list
        val currentList = _scheduleList.value.toMutableList()
        currentList.add(schedule)
        _scheduleList.value = currentList
        // Save the updated list to SharedPreferences
        saveSchedulesToPreferences(currentList)
    }

    private fun saveSchedulesToPreferences(scheduleList: List<Schedule>) {
        val json = gson.toJson(scheduleList)
        sharedPreferences.edit() { putString(scheduleListKey, json) }
    }

    fun showDialog() {
        _showDialog.value = true
    }

    fun hideDialog() {
        _showDialog.value = false
    }
}