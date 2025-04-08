package com.example.appscheduler.ui.screens

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.appscheduler.data.models.Schedule
import com.example.appscheduler.ui.SharedViewModel
import com.example.appscheduler.ui.components.AddSchedule
import com.example.appscheduler.ui.components.DeleteSchedule
import com.example.appscheduler.ui.components.EditSchedule
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    navController: NavController, sharedViewModel: SharedViewModel
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val scheduleList by viewModel.scheduleList.collectAsState()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("Permission", "Notification permission granted")
        } else {
            Log.d("Permission", "Notification permission denied")
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
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add App Scheduler")
        }
    })
}

fun createNotificationChannel(context: Context) {
    val channelId = "channel_id"
    val name = "My Channel"
    val descriptionText = "Channel for launching other apps"
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
    context: Context,
    launcher: ManagedActivityResultLauncher<String, Boolean>
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun ScheduleList(scheduleList: List<Schedule>, viewModel: HomeViewModel) {
    LazyColumn {
        items(scheduleList.size) { scheduleIndex ->
            ScheduleListItem(schedule = scheduleList[scheduleIndex], viewModel)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScheduleListItem(schedule: Schedule, viewModel: HomeViewModel) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
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
