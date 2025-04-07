package com.example.appscheduler.ui.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    val alarmTimes by viewModel.scheduleList.collectAsState()

    Scaffold(content = { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AddSchedule(viewModel)
            EditSchedule(viewModel)
            DeleteSchedule(viewModel)
            ScheduleList(alarmTimes = alarmTimes, viewModel)

        }
    }, floatingActionButton = {
        FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add App Scheduler")
        }
    })
}

@Composable
fun ScheduleList(alarmTimes: List<Schedule>, viewModel: HomeViewModel) {
    LazyColumn {
        items(alarmTimes.size) { alarmTime ->
            ScheduleListItem(alarmTime = alarmTimes[alarmTime], viewModel)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScheduleListItem(alarmTime: Schedule, viewModel: HomeViewModel) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val formattedDateTime = alarmTime.scheduledTime.format(formatter)
    Row(
        modifier = Modifier
            .padding(8.dp)
            .combinedClickable(onClick = { viewModel.showEditDialog(alarmTime) }, onLongClick = {
                viewModel.showDeleteDialog(alarmTime)
            })
    ) {
        Text(
            text = formattedDateTime, modifier = Modifier.padding(8.dp)
        )
        Text(text = alarmTime.packageName, modifier = Modifier.padding(8.dp))
    }
}
