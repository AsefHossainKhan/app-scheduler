package com.example.appscheduler.data.models

import java.time.LocalDateTime
import java.util.UUID

data class Logger(
    var packageName: String,
    var scheduledTime: String,
    val executionTime: LocalDateTime,
    var successfullyExecuted: Boolean = false,
    var id: UUID = UUID.randomUUID()
)