package com.example.appscheduler.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.example.appscheduler.R
import com.example.appscheduler.ui.screens.HomeViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSchedule(viewModel: HomeViewModel) {
    val showDialog by viewModel.showEditDialog.collectAsState(initial = false)
    val context = LocalContext.current
    val packageManager = context.packageManager

    if (showDialog) {
        var datePicked by remember { mutableStateOf(viewModel.scheduleItem.value.scheduledTime.toLocalDate()) }
        var timePicked by remember { mutableStateOf(viewModel.scheduleItem.value.scheduledTime.toLocalTime()) }
        var selectedAppName by remember { mutableStateOf(viewModel.scheduleItem.value.packageName) } // Store selected app name

        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }
        var showAppPicker by remember { mutableStateOf(false) }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = datePicked.atStartOfDay(ZoneId.systemDefault()).toInstant()
                .toEpochMilli()
        )
        val timePickerState = rememberTimePickerState(
            initialHour = timePicked.hour, initialMinute = timePicked.minute, is24Hour = false
        )

        Dialog(onDismissRequest = { viewModel.hideEditDialog() }) {
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
                                Text(text = stringResource(R.string.ok))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text(text = stringResource(R.string.cancel))
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
                            Text(text = stringResource(R.string.ok))
                        }
                    }, dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text(text = stringResource(R.string.cancel))
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
                ElevatedButton(
                    modifier = Modifier.fillMaxWidth(), onClick = { showDatePicker = true }) {
                    Text(text = stringResource(R.string.select_date, datePicked))
                }
                ElevatedButton(
                    modifier = Modifier.fillMaxWidth(), onClick = { showTimePicker = true }) {
                    Text(
                        text = stringResource(
                            R.string.select_time, timePicked.hour, timePicked.minute
                        )
                    )
                }
                ElevatedButton(
                    modifier = Modifier.fillMaxWidth(), onClick = { showAppPicker = true }) {
                    Text(
                        text = stringResource(
                            R.string.select_app, packageManager.getPackageInfo(
                                selectedAppName.toString(), 0
                            ).applicationInfo?.loadLabel(packageManager).toString()
                        )
                    )
                }

                Button(modifier = Modifier.align(Alignment.CenterHorizontally), onClick = {
                    val selectedDateTime = LocalDateTime.of(datePicked, timePicked)
                    if (selectedDateTime.isBefore(LocalDateTime.now())) {
                        Toast.makeText(
                            context, context.getString(R.string.time_validation), Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    if (viewModel.checkConflictInSchedule(selectedDateTime)) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.conflict_validation), Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    if (selectedAppName == "null") {
                        Toast.makeText(context,
                            context.getString(R.string.app_select_validation), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.editSchedule(
                        schedule = viewModel.scheduleItem.value, selectedDateTime, selectedAppName
                    )
                    viewModel.clearSchedule()
                    viewModel.hideEditDialog()
                }) {
                    Text(stringResource(R.string.edit))
                }
            }
        }
    }
}