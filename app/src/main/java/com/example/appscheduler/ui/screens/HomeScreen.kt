package com.example.appscheduler.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.appscheduler.data.models.Schedule
import com.example.appscheduler.ui.SharedViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel
) {
    val viewModel: AlarmViewModel = hiltViewModel()
    val alarmTimes by viewModel.scheduleList.collectAsState()

    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AlarmContent(viewModel)
                ScheduleList(alarmTimes = alarmTimes)

            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showDialog() }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add App Scheduler")
            }
        }
    )
}

@Composable
fun ScheduleList(alarmTimes: List<Schedule>) {
    LazyColumn {
        items(alarmTimes.size) { alarmTime ->
            ScheduleListItem(alarmTime = alarmTimes[alarmTime])
        }
    }
}

@Composable
fun ScheduleListItem(alarmTime: Schedule) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val formattedDateTime = alarmTime.scheduledTime.format(formatter)
    Row {
        Text(
            text = formattedDateTime,
            modifier = Modifier.padding(8.dp)
        )
        Text(text = alarmTime.packageName, modifier = Modifier.padding(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmContent(viewModel: AlarmViewModel) {
    val alarmTime by viewModel.scheduleItem.collectAsState(initial = null)
    val showDialog by viewModel.showDialog.collectAsState(initial = false)
    var datePicked by remember { mutableStateOf(LocalDate.now()) }
    var timePicked by remember { mutableStateOf(LocalTime.now()) }
    val context = LocalContext.current
    var selectedAppName by remember { mutableStateOf<String?>(null) } // Store selected app name

    if (showDialog) {
        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }
        var showAppPicker by remember { mutableStateOf(false) }
        val datePickerState = rememberDatePickerState()
        val timePickerState = rememberTimePickerState()

        Dialog(onDismissRequest = { viewModel.hideDialog() }) {
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
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                                }
                            }) {
                                Text(text = "OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text(text = "Cancel")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
                if (showTimePicker) {
                    AlertDialog(
                        onDismissRequest = { showTimePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showTimePicker = false
                                timePicked =
                                    LocalTime.of(timePickerState.hour, timePickerState.minute)
                            }) {
                                Text(text = "OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTimePicker = false }) {
                                Text(text = "Cancel")
                            }
                        },
                        text = {
                            TimePicker(state = timePickerState)
                        }
                    )
                }
                // Add app picker
                if (showAppPicker) {
                    ShowInstalledAppsScreen(
                        onAppSelected = { appName ->
                            selectedAppName = appName
                            showAppPicker = false
                        },
                        onDismiss = {
                            showAppPicker = false
                        }
                    )
                }
                TextButton(onClick = { showDatePicker = true }) {
                    Text(text = "Select Date: ${datePicked}")
                }
                TextButton(onClick = { showTimePicker = true }) {
                    Text(text = "Select Time: ${timePicked.hour}:${timePicked.minute}")
                }
                TextButton(onClick = { showAppPicker = true }) {
                    Text(text = "Select App: ${selectedAppName ?: "None"}") // Display selected app name
                }

                Button(onClick = {
                    val selectedDateTime = LocalDateTime.of(datePicked, timePicked)
                    if (selectedDateTime.isBefore(LocalDateTime.now())) {
                        Toast.makeText(
                            context,
                            "Please pick a future date and time",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    Log.d("TAG", "AlarmContent: $selectedAppName")
                    if (selectedAppName == null || selectedAppName == "null") {
                        Toast.makeText(context, "Please select an app", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val schedule = Schedule(selectedAppName!!, selectedDateTime)
                    viewModel.saveSchedule(schedule = schedule)
                    viewModel.hideDialog()
                }) {
                    Text("Save")
                }
            }
        }
    }
}
