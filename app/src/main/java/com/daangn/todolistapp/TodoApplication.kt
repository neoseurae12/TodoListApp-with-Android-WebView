package com.daangn.todolistapp

import android.app.Application
import com.daangn.todolistapp.repository.TodoRepository

class TodoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // TodoRepository 초기화
        TodoRepository.initialize(this)
    }
}