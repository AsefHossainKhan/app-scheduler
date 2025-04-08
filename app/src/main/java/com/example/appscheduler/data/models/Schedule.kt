package com.example.appscheduler.data.models

import java.time.LocalDateTime
import java.util.UUID

data class Schedule(
    var packageName: String,
    var scheduledTime: LocalDateTime,
    var id: UUID = UUID.randomUUID()
)