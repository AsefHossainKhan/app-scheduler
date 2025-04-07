package com.example.appscheduler.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.appscheduler.ui.screens.HomeViewModel

@Composable
fun DeleteSchedule(viewModel: HomeViewModel) {
    val showDeleteConfirmation by viewModel.showDeleteDialog.collectAsState(initial = false)
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            title = { Text("Delete Schedule") },
            text = { Text("Are you sure you want to delete this schedule?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSchedule(viewModel.scheduleItem.value)
                    viewModel.hideDeleteDialog()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.hideDeleteDialog()
                }) {
                    Text("Cancel")
                }
            })
    }
}