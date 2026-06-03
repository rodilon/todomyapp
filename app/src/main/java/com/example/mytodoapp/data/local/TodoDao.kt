package com.example.mytodoapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mytodoapp.data.model.Todo
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos ORDER BY created_at DESC")
    fun getAllTodos(): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Int): Todo?

    @Query("SELECT * FROM todos WHERE completed = 0 ORDER BY created_at DESC")
    fun getActiveTodos(): Flow<List<Todo>>

    @Query("SELECT COUNT(*) FROM todos WHERE completed = 1")
    fun getCompletedCount(): Flow<Int>

    @Insert
    suspend fun insertTodo(todo: Todo): Long

    @Update
    suspend fun updateTodo(todo: Todo)

    @Delete
    suspend fun deleteTodo(todo: Todo)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteTodoById(id: Int)

    @Query("DELETE FROM todos")
    suspend fun clearAllTodos()

    @Query("UPDATE todos SET completed = :completed WHERE id = :id")
    suspend fun updateTodoCompletion(id: Int, completed: Boolean)
}