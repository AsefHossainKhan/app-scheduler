package com.example.appscheduler.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import com.example.appscheduler.data.models.Schedule
import com.example.appscheduler.ui.screens.HomeViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSchedule(viewModel: HomeViewModel) {
    val showDialog by viewModel.showAddDialog.collectAsState(initial = false)
    val context = LocalContext.current

    if (showDialog) {
        var selectedAppName by remember { mutableStateOf<String?>(null) }
        var datePicked by remember { mutableStateOf(LocalDate.now()) }
        var timePicked by remember { mutableStateOf(LocalTime.now()) }
        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }
        var showAppPicker by remember { mutableStateOf(false) }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = datePicked.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        val timePickerState = rememberTimePickerState(
            initialHour = timePicked.hour, initialMinute = timePicked.minute, is24Hour = false
        )

        Dialog(onDismissRequest = { viewModel.hideAddDialog() }) {
            Column {
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showDatePicker = false
                                if (datePickerState.selectedDateMillis != null) {
                                    datePicked =
                                        Instant.ofEpochMilli(datePickerState.selectedDateMillis!!)
                                            .atZone(ZoneId.systemDefault()).toLocalDate()
                                }
                            }) {
                                Text(text = "OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text(text = "Cancel")
                            }
                        }) {
                        DatePicker(state = datePickerState)
                    }
                }
                if (showTimePicker) {
                    AlertDialog(onDismissRequest = { showTimePicker = false }, confirmButton = {
                        TextButton(onClick = {
                            showTimePicker = false
                            timePicked = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        }) {
                            Text(text = "OK")
                        }
                    }, dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text(text = "Cancel")
                        }
                    }, text = {
                        TimePicker(state = timePickerState)
                    })
                }
                if (showAppPicker) {
                    ShowInstalledApps(onAppSelected = { appName ->
                        selectedAppName = appName
                        showAppPicker = false
                    }, onDismiss = {
                        showAppPicker = false
                    })
                }
                TextButton(onClick = { showDatePicker = true }) {
                    Text(text = "Select Date: $datePicked")
                }
                TextButton(onClick = { showTimePicker = true }) {
                    Text(text = "Select Time: ${timePicked.hour}:${timePicked.minute}")
                }
                TextButton(onClick = { showAppPicker = true }) {
                    Text(text = "Select App: ${selectedAppName ?: "None"}")
                }

                Button(onClick = {
                    val selectedDateTime = LocalDateTime.of(datePicked, timePicked)
                    if (selectedDateTime.isBefore(LocalDateTime.now())) {
                        Toast.makeText(
                            context, "Please pick a future date and time", Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    if (viewModel.checkConflictInSchedule(selectedDateTime)){
                        Toast.makeText(
                            context, "Conflict with another schedule", Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    if (selectedAppName == null || selectedAppName == "null") {
                        Toast.makeText(context, "Please select an app", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val schedule = Schedule(selectedAppName!!, selectedDateTime)
                    viewModel.saveSchedule(schedule = schedule)
                    viewModel.clearSchedule()
                    viewModel.hideAddDialog()
                }) {
                    Text("Save")
                }
            }
        }
    }
}