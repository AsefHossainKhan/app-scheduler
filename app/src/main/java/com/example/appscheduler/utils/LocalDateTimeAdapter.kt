package com.example.appscheduler.utils

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(formatter.format(value))
        }
    }

    override fun read(inReader: JsonReader): LocalDateTime? {
        if (inReader.peek() == com.google.gson.stream.JsonToken.NULL) {
            inReader.nextNull()
            return null
        }
        val dateTimeString = inReader.nextString()
        return LocalDateTime.parse(dateTimeString, formatter)
    }
}