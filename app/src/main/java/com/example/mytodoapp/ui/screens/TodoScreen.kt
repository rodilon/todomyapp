// com/example/mytodoapp/ui/screens/TodoScreen.kt
package com.example.mytodoapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mytodoapp.data.model.Todo
import com.example.mytodoapp.ui.viewmodel.TodoViewModel
import com.example.mytodoapp.ui.viewmodel.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    viewModel: TodoViewModel = hiltViewModel()
) {
    val todos by viewModel.filteredTodos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTodoTitle by remember { mutableStateOf("") }
    var newTodoDescription by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar snackbar quando houver evento de UI
    LaunchedEffect(uiEvent) {
        uiEvent?.let { event ->
            val result = snackbarHostState.showSnackbar(
                message = when (event) {
                    is UiEvent.TodoAdded -> event.message
                    is UiEvent.TodoDeleted -> event.message
                    is UiEvent.TodoUpdated -> event.message
                },
                actionLabel = "Desfazer",
                duration = SnackbarDuration.Short
            )

            if (result == SnackbarResult.ActionPerformed) {
                when (event) {
                    is UiEvent.TodoDeleted -> viewModel.undoDelete(event.todo)
                    is UiEvent.TodoUpdated -> viewModel.undoUpdate(event.todo)
                    is UiEvent.TodoAdded -> viewModel.undoAdd(event.todo)
                }
            }
            viewModel.clearUiEvent()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = {
                                Text(
                                    "Pesquisar tarefas...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    } else {
                        Column {
                            Text("Minhas Tarefas", style = MaterialTheme.typography.headlineSmall)
                            Text(
                                "${todos.size} total • $completedCount concluídas",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                actions = {
                    if (isSearchActive) {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.setSearchQuery("")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar busca",
                                tint = Color.White
                            )
                        }
                    } else {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Pesquisar",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar tarefa")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Estado de carregamento
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Erro
            error?.let {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.clearError() }) {
                        Text("Descartar")
                    }
                }
            }

            // Lista de tarefas
            if (todos.isNotEmpty() && !isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Filtros
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.setFilter(null) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Todas (${todos.size})")
                        }
                        Button(
                            onClick = { viewModel.setFilter(false) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ativas")
                        }
                        Button(
                            onClick = { viewModel.setFilter(true) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Concluídas")
                        }
                    }

                    // Lista
                    LazyColumn {
                        items(todos, key = { it.id }) { todo ->

                            // create a documentation
                            val dismissState = rememberSwipeToDismissBoxState(
                                positionalThreshold = {totalDistance -> totalDistance * 0.7f},
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.deleteTodo(todo)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismissBox(state = dismissState, backgroundContent = {}) {
                                TodoItem(
                                    todo = todo,
                                    onDelete = { viewModel.deleteTodo(it) },
                                    onToggleCompletion = {
                                        viewModel.toggleTodoCompletion(it)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Estado vazio
            if (todos.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Nenhuma tarefa encontrada",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Clique no botão + para adicionar uma nova tarefa",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Dialog para adicionar nova tarefa
    if (showAddDialog) {
        AddTodoDialog(
            onDismiss = {
                showAddDialog = false
                newTodoTitle = ""
                newTodoDescription = ""
            },
            onAdd = { title, description ->
                viewModel.addTodo(title, description)
                showAddDialog = false
                newTodoTitle = ""
                newTodoDescription = ""
            },
            title = newTodoTitle,
            description = newTodoDescription,
            onTitleChange = { newTodoTitle = it },
            onDescriptionChange = { newTodoDescription = it }
        )
    }
}

@Composable
fun TodoItem(
    todo: Todo,
    onDelete: (Todo) -> Unit,
    onToggleCompletion: (Todo) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (todo.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Checkbox
            IconButton(
                onClick = { onToggleCompletion(todo) },
                modifier = Modifier.padding(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Marcar como concluído",
                    tint = if (todo.isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            }

            // Conteúdo
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (todo.isCompleted) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    color = if (todo.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                if (todo.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todo.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Delete button
            IconButton(
                onClick = { onDelete(todo) },
                modifier = Modifier.padding(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Deletar tarefa",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddTodoDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit,
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    // Você pode usar AlertDialog do Material 3
    // Para simplificar, vou usar um Box com animação

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Nova Tarefa",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Descrição (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.padding(8.dp))
                    Button(
                        onClick = { onAdd(title, description) },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Adicionar")
                    }
                }
            }
        }
    }
}
