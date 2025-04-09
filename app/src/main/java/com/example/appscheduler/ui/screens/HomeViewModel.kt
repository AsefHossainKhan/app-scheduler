package com.example.appscheduler.ui.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.example.appscheduler.data.models.Schedule
import com.example.appscheduler.utils.AlarmScheduler
import com.example.appscheduler.utils.Constants
import com.example.appscheduler.utils.LocalDateTimeAdapter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context, private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _scheduleList = MutableStateFlow<List<Schedule>>(emptyList())
    private val _scheduleItem = MutableStateFlow(Schedule("", LocalDateTime.now()))
    private val _showAddDialog = MutableStateFlow(false)
    private val _showEditDialog = MutableStateFlow(false)
    private val _showDeleteDialog = MutableStateFlow(false)

    val scheduleList = _scheduleList.asStateFlow()
    val scheduleItem = _scheduleItem.asStateFlow()
    val showAddDialog = _showAddDialog.asStateFlow()
    val showEditDialog = _showEditDialog.asStateFlow()
    val showDeleteDialog = _showDeleteDialog.asStateFlow()

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        Constants.SharedPreferences.APP_SCHEDULER_SHARED_PREF, Context.MODE_PRIVATE
    )
    private val gson = GsonBuilder().registerTypeAdapter(
        LocalDateTime::class.java, LocalDateTimeAdapter()
    ).create()

    init {
        loadScheduleListFromPreferences()
    }

    internal fun loadScheduleListFromPreferences() {
        val json = sharedPreferences.getString(Constants.SharedPreferences.SCHEDULE_LIST_KEY, null)
        if (json != null) {
            val type = object : TypeToken<List<Schedule>>() {}.type
            val scheduleList = gson.fromJson<List<Schedule>>(json, type)
            _scheduleList.value = scheduleList
        }
    }

    fun saveSchedule(schedule: Schedule) {
        _scheduleItem.value = schedule
        val currentList = _scheduleList.value.toMutableList()
        currentList.add(schedule)
        _scheduleList.value = currentList
        saveSchedulesToPreferences(currentList)
        alarmScheduler.scheduleAppLaunch(schedule)
    }

    fun checkConflictInSchedule(dateTime: LocalDateTime): Boolean {
        val currentList = _scheduleList.value
        return currentList.any { it.scheduledTime == dateTime }
    }

    fun clearSchedule() {
        _scheduleItem.value = Schedule("", LocalDateTime.now())
    }

    fun editSchedule(schedule: Schedule, updatedTime: LocalDateTime, updatedPackageName: String) {
        val currentList = _scheduleList.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == schedule.id }
        if (index != -1) {
            val updatedSchedule =
                schedule.copy(scheduledTime = updatedTime, packageName = updatedPackageName)
            val newList = currentList.toMutableList().apply {
                set(index, updatedSchedule)
            }
            _scheduleList.value = newList
            saveSchedulesToPreferences(newList)
            alarmScheduler.scheduleAppLaunch(updatedSchedule)
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        alarmScheduler.cancelAppLaunch(schedule)
        val currentList = _scheduleList.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == schedule.id }
        if (index != -1) {
            val newList = currentList.toMutableList().apply {
                removeAt(index)
            }
            _scheduleList.value = newList
            saveSchedulesToPreferences(newList)
        }
    }

    private fun saveSchedulesToPreferences(scheduleList: List<Schedule>) {
        val json = gson.toJson(scheduleList)
        sharedPreferences.edit { putString(Constants.SharedPreferences.SCHEDULE_LIST_KEY, json) }
    }

    fun showAddDialog() {
        _showAddDialog.value = true
    }

    fun hideAddDialog() {
        _showAddDialog.value = false
    }

    fun showEditDialog(schedule: Schedule) {
        _scheduleItem.value = schedule
        _showEditDialog.value = true
    }

    fun hideEditDialog() {
        _showEditDialog.value = false
    }

    fun showDeleteDialog(schedule: Schedule) {
        _scheduleItem.value = schedule
        _showDeleteDialog.value = true
    }

    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
    }
}