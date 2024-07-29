package com.daangn.todolistapp.repository

import android.content.Context
import androidx.room.Room
import com.daangn.todolistapp.data.TodoDatabase
import com.daangn.todolistapp.model.TodoEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

private const val DATABASE_NAME = "todo-database"

class TodoRepository private constructor(context: Context) {

    private val database : TodoDatabase = Room.databaseBuilder(
        context.applicationContext,
        TodoDatabase::class.java,
        DATABASE_NAME
    ).build()
    private val todoDao = database.todoDao()

    fun getTodos(): Flow<List<TodoEntity>> = todoDao.getTodos()

    suspend fun getTodo(id: UUID): TodoEntity = todoDao.getTodo(id)

    suspend fun updateTodo(todo: TodoEntity) {
        todoDao.updateTodo(todo)
    }

    suspend fun insertTodo(todo: TodoEntity) {
        todoDao.insertTodo(todo)
    }

    suspend fun deleteTodo(todo: TodoEntity) {
        todoDao.deleteTodo(todo)
    }

    suspend fun deleteAllTodos() {
        todoDao.deleteAllTodos()
    }

    companion object {
        private var INSTANCE: TodoRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = TodoRepository(context)
            }
        }

        fun get(): TodoRepository {
            return INSTANCE ?:
            throw IllegalStateException("TodoRepository must be initialized")
        }
    }
}