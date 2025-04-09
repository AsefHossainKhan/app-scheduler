package com.example.appscheduler.ui.screens

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appscheduler.R
import com.example.appscheduler.data.models.Schedule
import com.example.appscheduler.ui.components.AddSchedule
import com.example.appscheduler.ui.components.DeleteSchedule
import com.example.appscheduler.ui.components.EditSchedule
import com.example.appscheduler.utils.Constants
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = hiltViewModel()
    val scheduleList by viewModel.scheduleList.collectAsState()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                context,
                context.getString(R.string.notification_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.loadScheduleListFromPreferences()
        createNotificationChannel(context)
        requestExactAlarmPermission(context)
        requestNotificationPermission(context, launcher)
    }

    Scaffold(content = { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AddSchedule(viewModel)
            EditSchedule(viewModel)
            DeleteSchedule(viewModel)
            ScheduleList(scheduleList = scheduleList, viewModel)

        }
    }, floatingActionButton = {
        FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_fab_description))
        }
    })
}

fun createNotificationChannel(context: Context) {
    val channelId = Constants.Notification.CHANNEL_ID
    val name = Constants.Notification.CHANNEL_NAME
    val descriptionText = Constants.Notification.CHANNEL_DESCRIPTION
    val importance = NotificationManager.IMPORTANCE_HIGH

    val channel = NotificationChannel(channelId, name, importance).apply {
        description = descriptionText
    }

    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

fun requestExactAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = "package:${context.packageName}".toUri()
            }
            context.startActivity(intent)
        }
    }
}

fun requestNotificationPermission(
    context: Context, launcher: ManagedActivityResultLauncher<String, Boolean>
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun ScheduleList(scheduleList: List<Schedule>, viewModel: HomeViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)
    ) {
        items(scheduleList.size) { scheduleIndex ->
            ScheduleListItem(schedule = scheduleList[scheduleIndex], viewModel)
        }
        if (scheduleList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_apps_scheduled),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScheduleListItem(schedule: Schedule, viewModel: HomeViewModel) {
    val formatter = DateTimeFormatter.ofPattern(Constants.Formatting.DATE_TIME_FORMAT)
    val formattedDateTime = schedule.scheduledTime.format(formatter)
    val packageManager = LocalContext.current.packageManager
    val packageInfo = packageManager.getPackageInfo(schedule.packageName, 0)
    val appName = packageInfo.applicationInfo?.loadLabel(packageManager).toString()
    Row(
        modifier = Modifier
            .padding(8.dp)
            .combinedClickable(onClick = { viewModel.showEditDialog(schedule) }, onLongClick = {
                viewModel.showDeleteDialog(schedule)
            })
    ) {
        Text(
            text = formattedDateTime, modifier = Modifier.padding(8.dp)
        )
        Text(text = appName, modifier = Modifier.padding(8.dp))
    }
}
