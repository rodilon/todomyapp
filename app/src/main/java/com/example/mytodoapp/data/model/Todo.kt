// com/example/mytodoapp/data/model/Todo.kt
package com.example.mytodoapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @SerialName("title")
    val title: String,
    
    @SerialName("description")
    val description: String = "",
    
    @SerialName("completed")
    @ColumnInfo(name = "completed")
    val isCompleted: Boolean = false,
    
    @SerialName("created_at")
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerialName("updated_at")
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

// Classe de estado para UI (inclui loading, error, etc)
sealed class TodoState {
    object Loading : TodoState()
    data class Success(val todos: List<Todo>) : TodoState()
    data class Error(val message: String) : TodoState()
}

// Request/Response para API futura
@Serializable
data class CreateTodoRequest(
    val title: String,
    val description: String = ""
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String = ""
)
