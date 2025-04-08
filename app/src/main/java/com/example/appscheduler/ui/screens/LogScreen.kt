package com.example.appscheduler.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appscheduler.data.models.Logger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun LogScreen() {
    val viewModel: LoggerViewModel = hiltViewModel()
    val loggerList by viewModel.loggerList.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLoggerListFromPreferences()
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)
            ) {
                items(loggerList.size) { loggerIndex ->
                    LogItem(
                        logger = loggerList[loggerIndex],
                    )
                }
                if (loggerList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No logs available yet",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun LogItem(logger: Logger) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val scheduledTime = LocalDateTime.parse(logger.scheduledTime)
    val formattedScheduledTime = scheduledTime.format(formatter)
    val executionTime = logger.executionTime.format(formatter)
    val packageManager = LocalContext.current.packageManager
    val packageInfo = packageManager.getPackageInfo(logger.packageName, 0)
    val appName = packageInfo.applicationInfo?.loadLabel(packageManager).toString()
    Row {
        Text(text = "${executionTime}: $appName scheduled to execute at $formattedScheduledTime was ${if (logger.successfullyExecuted) "successfully executed" else "not executed"}")
    }
}