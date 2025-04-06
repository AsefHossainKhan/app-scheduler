package com.example.appscheduler.data.models

import java.time.LocalDateTime
import java.util.UUID

data class Schedule(
    val packageName: String,
    val scheduledTime: LocalDateTime,
    var isExecuted: Boolean = false,
    var id: UUID = UUID.randomUUID()
)