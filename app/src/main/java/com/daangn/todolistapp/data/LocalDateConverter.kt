package com.daangn.todolistapp.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class LocalDateConverter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun stringConverter(localDate: LocalDate): String {
        return localDate.format(formatter)
    }

    @TypeConverter
    fun localDateConverter(dateString: String): LocalDate {
        return LocalDate.parse(dateString, formatter)
    }
}