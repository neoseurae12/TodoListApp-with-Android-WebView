package com.daangn.todolistapp.di

import com.daangn.todolistapp.data.AppDatabase
import com.daangn.todolistapp.data.dao.TodoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {

    @Provides
    @Singleton
    fun provideTodoDao(appDatabase: AppDatabase): TodoDao = appDatabase.todoDao()
}