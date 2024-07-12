package com.daangn.todolistapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.daangn.todolistapp.data.dao.TodoDao
import com.daangn.todolistapp.model.TodoEntity

@Database(entities = [TodoEntity::class], version = 1)
@TypeConverters(TodoTypeConverters::class)
abstract class TodoDatabase: RoomDatabase() {

    abstract fun todoDao(): TodoDao
}