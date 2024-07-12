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
import java.time.LocalDate
import java.util.UUID

class MainViewModel : ViewModel() {

    private val todoRepository = TodoRepository.get()

    val todoListLiveData = todoRepository.getTodos()
    private val todoIdLiveData = MutableLiveData<UUID>()

    var todoLiveData: LiveData<TodoEntity?> =
        todoIdLiveData.switchMap { todoId ->
            todoRepository.getTodo(todoId)
        }

    var webAddButtonEnabled = true
    var hasInitialized = false

    fun loadTodo(todoId: UUID) {
        todoIdLiveData.postValue(todoId)
    }

    fun updateTodo(id: String, content: String, dueDate: String, isDone: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            loadTodo(UUID.fromString(id))
            todoLiveData.value?.let {
                it.content = content
                it.dueDate = LocalDate.parse(dueDate)
                it.isDone = isDone

                todoRepository.updateTodo(it)
            }
        }
    }

    fun updateTodoContent(todo: TodoEntity) {
        Log.d("todoToUpdate", "$todo")
        Log.d("content", "${todo.content}")

        viewModelScope.launch(Dispatchers.IO) {
            todoRepository.updateTodo(todo)
        }
    }

    fun updateTodoDueDate(todo: TodoEntity) {
        Log.d("todoToUpdate", "$todo")
        Log.d("dueDate", "${todo.dueDate}")

        viewModelScope.launch(Dispatchers.IO) {
            todoRepository.updateTodo(todo)
        }
    }

    fun updateTodoDone(todo: TodoEntity) {
        Log.d("todoToUpdate", "$todo")

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