package com.example.appscheduler.data.models

import java.util.Calendar
import java.util.UUID

data class Schedule(
    val packageName: String,
    val scheduledTime: Calendar,
    var isExecuted: Boolean = false,
    var id: UUID = UUID.randomUUID()
)