// com/example/mytodoapp/ui/viewmodel/TodoViewModel.kt
package com.example.mytodoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytodoapp.data.model.Todo
import com.example.mytodoapp.data.repository.ITodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: ITodoRepository
) : ViewModel() {
    
    // Estados principais
    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _completedCount = MutableStateFlow(0)
    val completedCount: StateFlow<Int> = _completedCount.asStateFlow()
    
    // Filtro de view
    private val _filterCompleted = MutableStateFlow<Boolean?>(null)
    val filterCompleted: StateFlow<Boolean?> = _filterCompleted.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Efeito colateral de UI (ex: mostrar snackbar)
    private val _uiEvent = MutableStateFlow<UiEvent?>(null)
    val uiEvent: StateFlow<UiEvent?> = _uiEvent.asStateFlow()
    
    init {
        loadTodos()
        observeCompletedCount()
    }

    private fun loadTodos() {
        viewModelScope.launch {
            _isLoading.value = true // Inicia loading para a carga inicial
            try {
                repository.getAllTodos().collect { todoList ->
                    _todos.value = todoList
                    _error.value = null
                    _isLoading.value = false // Para o loading assim que recebermos os dados
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao carregar todos"
                _isLoading.value = false
            }
        }
    }
    
    private fun observeCompletedCount() {
        viewModelScope.launch {
            repository.getCompletedCount().collect { count ->
                _completedCount.value = count
            }
        }
    }

    fun addTodo(title: String, description: String = "") {
        if (title.isBlank()) {
            _error.value = "Título não pode ser vazio"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true // Feedback visual durante a operação
            try {
                val newTodo = Todo(title = title, description = description)
                val id = repository.addTodo(newTodo)
                val addedTodo = newTodo.copy(id = id.toInt())
                _uiEvent.value = UiEvent.TodoAdded("Tarefa adicionada com sucesso!", addedTodo)
                clearError()
                // NÃO CHAME loadTodos() AQUI. O Flow do init já vai detectar a mudança.
            } catch (e: Exception) {
                _error.value = "Erro ao adicionar tarefa: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteTodo(todo)
                _uiEvent.value = UiEvent.TodoDeleted("Tarefa deletada!", todo)
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao deletar tarefa"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleTodoCompletion(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.updateTodoCompletion(todo.id, !todo.isCompleted)
                _uiEvent.value = UiEvent.TodoUpdated("Tarefa atualizada!", todo)
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao atualizar tarefa"
            }
        }
    }

    fun undoDelete(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.addTodo(todo)
            } catch (e: Exception) {
                _error.value = "Erro ao restaurar tarefa: ${e.message}"
            }
        }
    }

    fun undoUpdate(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.updateTodoCompletion(todo.id, todo.isCompleted)
            } catch (e: Exception) {
                _error.value = "Erro ao reverter alteração: ${e.message}"
            }
        }
    }

    fun undoAdd(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.deleteTodo(todo)
            } catch (e: Exception) {
                _error.value = "Erro ao remover tarefa: ${e.message}"
            }
        }
    }
    
    fun setFilter(completed: Boolean?) {
        _filterCompleted.value = completed
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val filteredTodos = combine(_todos, _filterCompleted, _searchQuery) { todos, filter, query ->
        val filteredByStatus = when (filter) {
            true -> todos.filter { it.isCompleted }
            false -> todos.filter { !it.isCompleted }
            null -> todos
        }
        
        if (query.isBlank()) {
            filteredByStatus
        } else {
            filteredByStatus.filter {
                it.title.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearUiEvent() {
        _uiEvent.value = null
    }
}

// Eventos de UI
sealed class UiEvent {
    data class TodoAdded(val message: String, val todo: Todo) : UiEvent()
    data class TodoDeleted(val message: String, val todo: Todo) : UiEvent()
    data class TodoUpdated(val message: String, val todo: Todo) : UiEvent()
}
