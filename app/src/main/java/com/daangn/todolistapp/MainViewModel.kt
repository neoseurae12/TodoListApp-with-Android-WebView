package com.daangn.todolistapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daangn.todolistapp.model.TodoEntity
import com.daangn.todolistapp.repository.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "MainViewModel"

class MainViewModel : ViewModel() {

    private val todoRepository = TodoRepository.get()

    private val _todos: MutableStateFlow<List<TodoEntity>> = MutableStateFlow(emptyList())
    val todos: StateFlow<List<TodoEntity>> = _todos.asStateFlow()

    private val _todo: MutableStateFlow<TodoEntity?> = MutableStateFlow(null)
    private val todo: StateFlow<TodoEntity?> = _todo.asStateFlow()

    private val _oldTodosReady = MutableStateFlow(false)
    val oldTodosReady: StateFlow<Boolean> = _oldTodosReady.asStateFlow()

    var webAddButtonEnabled = true

    init {
        viewModelScope.launch {
            todoRepository.getTodos().collect { collectedTodos ->
                _todos.value = collectedTodos

                _oldTodosReady.value = true

                // 디버깅: DB 의 투두 데이터들에 변경사항이 발생할 때마다, 그 시점의 DB 내 모든 투두 데이터들을 로그로 출력해 보임
                for (todo in todos.value) {
                    Log.d(TAG, "$todo")
                }
                Log.d(TAG, "Got ${todos.value.size} todo(s)")
            }
        }
    }

    private suspend fun getTodo(todoId: UUID) {
        _todo.value = todoRepository.getTodo(todoId)
        Log.d(TAG, "Get Todo: " + todo.value?.content)
    }

    fun updateTodo(todoId: String, onUpdate: (TodoEntity) -> TodoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            getTodo(UUID.fromString(todoId))

            _todo.update { oldTodo ->
                oldTodo?.let { onUpdate(it) }
            }

            todo.value?.let { todoRepository.updateTodo(it) }

            Log.d(TAG, "업데이트된 투두의 내용: ${todo.value?.content}")
        }
    }

    fun insertTodo(content: String): TodoEntity {
        val newTodo = TodoEntity(content = content)
        viewModelScope.launch(Dispatchers.IO) {
            todoRepository.insertTodo(newTodo)
        }
        Log.d(TAG, "New Todo's UUID: ${newTodo.id}")
        return newTodo
    }

    fun deleteTodo(todoId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            getTodo(UUID.fromString(todoId))

            Log.d(TAG, "Delete Todo: ${todo.value?.content}")

            todo.value?.let { todoRepository.deleteTodo(it) }
        }
    }

    fun deleteAllTodos() {
        viewModelScope.launch(Dispatchers.IO) {
            todoRepository.deleteAllTodos()
        }
    }
}