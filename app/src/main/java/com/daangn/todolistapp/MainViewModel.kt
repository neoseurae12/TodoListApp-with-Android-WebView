package com.daangn.todolistapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.daangn.todolistapp.model.TodoEntity
import com.daangn.todolistapp.repository.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "MainViewModel"

class MainViewModel : ViewModel() {

    private val todoRepository = TodoRepository.get()

    val todoListLiveData = todoRepository.getTodos()
    private val todoIdLiveData = MutableLiveData<UUID>()

    var todoLiveData: LiveData<TodoEntity?> =
        todoIdLiveData.switchMap { todoId ->
            todoRepository.getTodo(todoId)
        }

    var webAddButtonEnabled = true

    fun loadTodoById(todoId: UUID) {
        todoIdLiveData.postValue(todoId)
    }

    fun updateTodoContent(todo: TodoEntity) {
        Log.d(TAG, "업데이트된 투두: $todo")
        Log.d(TAG, "내용: ${todo.content}")

        viewModelScope.launch(Dispatchers.IO) {
            todoRepository.updateTodo(todo)
        }
    }

    fun updateTodoDueDate(todo: TodoEntity) {
        Log.d(TAG, "업데이트된 투두: $todo")
        Log.d(TAG, "마감일: ${todo.dueDate}")

        viewModelScope.launch(Dispatchers.IO) {
            todoRepository.updateTodo(todo)
        }
    }

    fun updateTodoDone(todo: TodoEntity) {
        Log.d(TAG, "업데이트된 투두: $todo")

        viewModelScope.launch(Dispatchers.IO) {
            todoRepository.updateTodo(todo)
        }
    }

    fun insertTodo(content: String): TodoEntity {
        val newTodo = TodoEntity(content = content)
        viewModelScope.launch(Dispatchers.IO) {
            todoRepository.insertTodo(newTodo)
        }
        return newTodo
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            todoRepository.deleteTodo(todo)
        }
    }

    fun deleteAllTodos() {
        viewModelScope.launch(Dispatchers.IO) {
            todoRepository.deleteAllTodos()
        }
    }
}