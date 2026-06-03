// com/example/mytodoapp/di/AppModule.kt
package com.example.mytodoapp.di

import android.content.Context
import androidx.room.Room
import com.example.mytodoapp.data.local.TodoDatabase
import com.example.mytodoapp.data.local.TodoDao
import com.example.mytodoapp.data.repository.ITodoRepository
import com.example.mytodoapp.data.repository.TodoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    /**
     * Fornece instância do banco de dados Room
     */
    @Singleton
    @Provides
    fun provideTodoDatabase(
        @ApplicationContext context: Context
    ): TodoDatabase {
        return TodoDatabase.getDatabase(context)
    }
    
    /**
     * Fornece o DAO do banco de dados
     */
    @Singleton
    @Provides
    fun provideTodoDao(database: TodoDatabase): TodoDao {
        return database.todoDao()
    }
    
    /**
     * Fornece a implementação do Repository
     * 
     * Quando adicionar API:
     * - Criar um módulo separado para APIs
     * - Fornecer aqui a implementação que combina local + remote
     */
    @Singleton
    @Provides
    fun provideTodoRepository(
        todoDao: TodoDao
    ): ITodoRepository {
        return TodoRepository(todoDao)
    }
}
