// com/example/mytodoapp/data/repository/TodoRepository.kt
package com.example.mytodoapp.data.repository

import com.example.mytodoapp.data.local.TodoDao
import com.example.mytodoapp.data.model.Todo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ITodoRepository {
    fun getAllTodos(): Flow<List<Todo>>
    fun getActiveTodos(): Flow<List<Todo>>
    fun getCompletedCount(): Flow<Int>
    suspend fun getTodoById(id: Int): Todo?
    suspend fun addTodo(todo: Todo): Long
    suspend fun updateTodo(todo: Todo)
    suspend fun deleteTodo(todo: Todo)
    suspend fun deleteTodoById(id: Int)
    suspend fun updateTodoCompletion(id: Int, completed: Boolean)
    suspend fun clearAllTodos()
}

/**
 * Implementação do Repository com dados locais
 * 
 * Quando integrar API:
 * 1. Criar TodoRemoteDataSource com Retrofit
 * 2. Adicionar lógica de sincronização aqui
 * 3. Implementar cache + network pattern
 */
class TodoRepository @Inject constructor(
    private val todoDao: TodoDao
    // private val todoRemoteDataSource: TodoRemoteDataSource // Adicionar depois
) : ITodoRepository {
    
    override fun getAllTodos(): Flow<List<Todo>> {
        // Quando adicionar API: implementar lógica de fetch e sincronização
        return todoDao.getAllTodos()
    }
    
    override fun getActiveTodos(): Flow<List<Todo>> {
        return todoDao.getActiveTodos()
    }
    
    override fun getCompletedCount(): Flow<Int> {
        return todoDao.getCompletedCount()
    }
    
    override suspend fun getTodoById(id: Int): Todo? {
        return todoDao.getTodoById(id)
    }
    
    override suspend fun addTodo(todo: Todo): Long {
        // Quando adicionar API: fazer POST para servidor e sincronizar
        return todoDao.insertTodo(todo)
    }
    
    override suspend fun updateTodo(todo: Todo) {
        // Quando adicionar API: fazer PUT para servidor
        todoDao.updateTodo(todo)
    }
    
    override suspend fun deleteTodo(todo: Todo) {
        // Quando adicionar API: fazer DELETE no servidor
        todoDao.deleteTodo(todo)
    }
    
    override suspend fun deleteTodoById(id: Int) {
        todoDao.deleteTodoById(id)
    }
    
    override suspend fun updateTodoCompletion(id: Int, completed: Boolean) {
        // Quando adicionar API: sincronizar com servidor
        todoDao.updateTodoCompletion(id, completed)
    }
    
    override suspend fun clearAllTodos() {
        todoDao.clearAllTodos()
    }
}
