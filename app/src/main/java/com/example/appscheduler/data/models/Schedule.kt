package com.example.appscheduler.data.models

import java.time.LocalDateTime
import java.util.UUID

data class Schedule(
    var packageName: String,
    var scheduledTime: LocalDateTime,
    var isExecuted: Boolean = false,
    var id: UUID = UUID.randomUUID()
)