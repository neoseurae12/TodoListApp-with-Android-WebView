package com.daangn.todolistapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.daangn.todolistapp.model.TodoEntity
import java.util.UUID

@Dao
interface TodoDao {

    @Query("SELECT * FROM Todo")
    fun getTodos(): LiveData<List<TodoEntity>>

    @Query("SELECT * FROM Todo WHERE id=(:id)")
    fun getTodo(id: UUID): LiveData<TodoEntity?>

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)

    @Query("DELETE FROM Todo")
    suspend fun deleteAllTodos()

}