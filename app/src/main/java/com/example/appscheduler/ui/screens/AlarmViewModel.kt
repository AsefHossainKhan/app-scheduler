package com.example.appscheduler.ui.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
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

    private val _alarmTimeList = MutableStateFlow<List<LocalDateTime>>(emptyList())
    private val _alarmTime = MutableStateFlow(LocalDateTime.now())
    private val _showDialog = MutableStateFlow(false)

    val alarmTimeList = _alarmTimeList.asStateFlow()
    val alarmTime = _alarmTime.asStateFlow()
    val showDialog = _showDialog.asStateFlow()

    private val alarmListKey = "alarm_list"
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(alarmListKey, Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        loadAlarmTimes()
    }

    private fun loadAlarmTimes() {
        val json = sharedPreferences.getString(alarmListKey, null)
        if (json != null) {
            val type = object : TypeToken<List<LocalDateTime>>() {}.type
            val alarmTimes = gson.fromJson<List<LocalDateTime>>(json, type)
            _alarmTimeList.value = alarmTimes
        }
    }
    fun saveAlarmTime(alarmTime: LocalDateTime) {
        _alarmTime.value = alarmTime
        // Add the new alarm time to the list
        val currentList = _alarmTimeList.value.toMutableList()
        currentList.add(alarmTime)
        _alarmTimeList.value = currentList
        // Save the updated list to SharedPreferences
        saveAlarmTimes(currentList)
    }

    private fun saveAlarmTimes(alarmTimes: List<LocalDateTime>) {
        val json = gson.toJson(alarmTimes)
        sharedPreferences.edit() { putString(alarmListKey, json) }
    }

    fun showDialog() {
        _showDialog.value = true
    }

    fun hideDialog() {
        _showDialog.value = false
    }
}