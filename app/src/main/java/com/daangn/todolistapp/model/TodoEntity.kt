package com.daangn.todolistapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.LocalDate

@Entity(tableName = "Todo")
data class TodoEntity(
    @PrimaryKey(true)
    val id: Int = 0,

    @ColumnInfo
    var content: String,

    @ColumnInfo
    var dueDate: LocalDate? = null,

    @ColumnInfo
    var isDone: Boolean = false
): Serializable