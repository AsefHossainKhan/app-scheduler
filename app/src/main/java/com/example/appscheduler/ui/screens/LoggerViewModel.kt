package com.example.appscheduler.ui.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.example.appscheduler.data.models.Logger
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
class LoggerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _loggerList = MutableStateFlow<List<Logger>>(emptyList())

    val loggerList = _loggerList.asStateFlow()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.SharedPreferences.APP_SCHEDULER_SHARED_PREF, Context.MODE_PRIVATE)
    private val gson = GsonBuilder().registerTypeAdapter(
        LocalDateTime::class.java, LocalDateTimeAdapter()
    ).create()

    init {
        loadLoggerListFromPreferences()
    }

    internal fun loadLoggerListFromPreferences() {
        val json = sharedPreferences.getString(Constants.SharedPreferences.LOGGER_KEY, null)
        if (json != null) {
            val type = object : TypeToken<List<Logger>>() {}.type
            val loggerList = gson.fromJson<List<Logger>>(json, type)
            _loggerList.value = loggerList
        }
    }
}