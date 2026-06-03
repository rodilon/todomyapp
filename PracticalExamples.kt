// EXEMPLOS PRÁTICOS: Como Estender o Projeto
// com/example/mytodoapp/examples/PracticalExamples.kt

package com.example.mytodoapp.examples

/**
 * EXEMPLO 1: Adicionar Busca/Filtro
 */

import androidx.compose.foundation.layout.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// No ViewModel:
// @HiltViewModel
// class TodoViewModel @Inject constructor(...) : ViewModel() {
//     private val _searchQuery = MutableStateFlow("")
//     val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
//     
//     fun setSearchQuery(query: String) {
//         _searchQuery.value = query
//     }
//     
//     val filteredTodos: StateFlow<List<Todo>> = combine(
//         todos,
//         searchQuery
//     ) { todos, query ->
//         if (query.isEmpty()) {
//             todos
//         } else {
//             todos.filter { 
//                 it.title.contains(query, ignoreCase = true) ||
//                 it.description.contains(query, ignoreCase = true)
//             }
//         }
//     }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
// }

// Na UI:
@Composable
fun TodoScreenWithSearch(viewModel: Any) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Column {
    //     OutlinedTextField(
    //         value = searchQuery,
    //         onValueChange = { 
    //             searchQuery = it
    //             // viewModel.setSearchQuery(it)
    //         },
    //         placeholder = { Text("Buscar tarefas...") },
    //         leadingIcon = {
    //             Icon(Icons.Default.Search, contentDescription = "Buscar")
    //         },
    //         modifier = Modifier
    //             .fillMaxWidth()
    //             .padding(16.dp)
    //     )
    //     
    //     // LazyColumn com resultados filtrados
    // }
}

/**
 * EXEMPLO 2: Swipe para Deletar
 */

// Implementar com Compose Foundation:
// LazyColumn {
//     items(todos, key = { it.id }) { todo ->
//         var dismissState by rememberDismissState(
//             confirmStateChange = { dismissValue ->
//                 if (dismissValue == DismissValue.DismissedToEnd) {
//                     viewModel.deleteTodo(todo)
//                     true
//                 } else {
//                     false
//                 }
//             }
//         )
//         
//         SwipeToDismiss(
//             state = dismissState,
//             background = { ... },
//             dismissContent = {
//                 TodoItem(todo)
//             }
//         )
//     }
// }

/**
 * EXEMPLO 3: Adicionar Drag & Drop para Reordenar
 */

// Use Lazycolumn com reorderingModule ou implemente manualmente:
// data class DragDropListState(
//     val todoList: List<Todo>,
//     val draggedTodo: Todo? = null,
//     val draggedOverIndex: Int? = null
// )
//
// Detectar gestos:
// Modifier
//     .pointerInput(Unit) {
//         detectDragGestures(
//             onDrag = { change, _ ->
//                 // Atualizar posição
//             }
//         )
//     }

/**
 * EXEMPLO 4: Adicionar Categorias/Tags
 */

// Estender o modelo:
// @Entity(tableName = "todos")
// data class Todo(
//     @PrimaryKey val id: Int = 0,
//     val title: String,
//     val description: String = "",
//     val category: String = "Default",
//     val tags: List<String> = emptyList(),
//     val isCompleted: Boolean = false,
//     val createdAt: Long = System.currentTimeMillis()
// )
//
// Query para buscar por categoria:
// @Query("SELECT * FROM todos WHERE category = :category")
// fun getTodosByCategory(category: String): Flow<List<Todo>>
//
// UI:
// val categories = listOf("Work", "Personal", "Shopping")
// Row {
//     categories.forEach { category ->
//         FilterChip(
//             selected = selectedCategory == category,
//             onClick = { selectedCategory = category },
//             label = { Text(category) }
//         )
//     }
// }

/**
 * EXEMPLO 5: Adicionar Notificações Locais (Lembretes)
 */

// Usar WorkManager para agendar:
// class TodoReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
//     override suspend fun doWork(): Result {
//         val todoId = inputData.getInt("todo_id", -1)
//         
//         // Mostrar notificação
//         val notification = NotificationCompat.Builder(applicationContext, "todo_channel")
//             .setSmallIcon(R.drawable.ic_notification)
//             .setContentTitle("Lembrete de Tarefa")
//             .setContentText("Você tem uma tarefa pendente")
//             .build()
//         
//         NotificationManagerCompat.from(applicationContext)
//             .notify(todoId, notification)
//         
//         return Result.success()
//     }
// }
//
// Agendar no ViewModel:
// fun setReminder(todo: Todo, minutesFromNow: Int) {
//     val workRequest = OneTimeWorkRequestBuilder<TodoReminderWorker>()
//         .setInitialDelay(minutesFromNow.toLong(), TimeUnit.MINUTES)
//         .setInputData(workDataOf("todo_id" to todo.id))
//         .build()
//     
//     WorkManager.getInstance(context).enqueueUniqueWork(
//         "reminder_${todo.id}",
//         ExistingWorkPolicy.REPLACE,
//         workRequest
//     )
// }

/**
 * EXEMPLO 6: Adicionar Análise de Produtividade
 */

// Estender ViewModel:
// class TodoViewModel(...) : ViewModel() {
//     val completedToday: StateFlow<Int> = repository.getCompletedCount()
//         .stateIn(viewModelScope, SharingStarted.Lazily, 0)
//     
//     val completionRate: StateFlow<Float> = combine(
//         todos,
//         completedToday
//     ) { todos, completed ->
//         if (todos.isEmpty()) 0f else (completed.toFloat() / todos.size)
//     }.stateIn(viewModelScope, SharingStarted.Lazily, 0f)
//     
//     val productivityTrend: StateFlow<List<DailyStats>> = ...
// }
//
// UI com gráfico (usar Compose Charts ou vega):
// @Composable
// fun ProductivityScreen(viewModel: TodoViewModel) {
//     val completionRate by viewModel.completionRate.collectAsState()
//     
//     LinearProgressIndicator(
//         progress = completionRate,
//         modifier = Modifier
//             .fillMaxWidth()
//             .padding(16.dp)
//     )
//     
//     Text("Taxa de Conclusão: ${(completionRate * 100).toInt()}%")
// }

/**
 * EXEMPLO 7: Adicionar Temas Customizados
 */

// Estender Theme.kt:
// enum class ThemeMode {
//     LIGHT, DARK, SYSTEM
// }
//
// @Composable
// fun MyTodoAppTheme(
//     themeMode: ThemeMode = ThemeMode.SYSTEM,
//     content: @Composable () -> Unit
// ) {
//     val darkTheme = when (themeMode) {
//         ThemeMode.DARK -> true
//         ThemeMode.LIGHT -> false
//         ThemeMode.SYSTEM -> isSystemInDarkTheme()
//     }
//     
//     // ... resto do código
// }

/**
 * EXEMPLO 8: Adicionar Sincronização em Background
 */

// Worker para sincronizar com servidor:
// class TodoSyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
//     @Inject lateinit var repository: TodoRepository
//     
//     override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
//         return@withContext try {
//             // Sincronizar todos com servidor
//             repository.syncWithServer()
//             Result.success()
//         } catch (e: Exception) {
//             Result.retry()
//         }
//     }
// }
//
// Agendar sincronização periódica:
// val syncWorkRequest = PeriodicWorkRequestBuilder<TodoSyncWorker>(
//     15, TimeUnit.MINUTES
// ).build()
//
// WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//     "todo_sync",
//     ExistingPeriodicWorkPolicy.KEEP,
//     syncWorkRequest
// )

/**
 * EXEMPLO 9: Adicionar Undo/Redo
 */

// Data class para histórico:
// data class TodoAction(
//     val action: ActionType,
//     val todo: Todo,
//     val timestamp: Long = System.currentTimeMillis()
// )
//
// enum class ActionType { ADD, UPDATE, DELETE }
//
// No ViewModel:
// private val _history = MutableStateFlow<List<TodoAction>>(emptyList())
// private var historyIndex = -1
//
// fun undo() {
//     if (historyIndex > 0) {
//         historyIndex--
//         applyHistoryState()
//     }
// }
//
// fun redo() {
//     if (historyIndex < _history.value.size - 1) {
//         historyIndex++
//         applyHistoryState()
//     }
// }

/**
 * EXEMPLO 10: Adicionar Compartilhamento de Tarefas
 */

// Action para compartilhar:
// fun shareTodo(todo: Todo) {
//     val shareText = """
//         ${todo.title}
//         ${todo.description}
//         Status: ${if (todo.isCompleted) "Concluído" else "Pendente"}
//     """.trimIndent()
//     
//     val shareIntent = Intent().apply {
//         action = Intent.ACTION_SEND
//         putExtra(Intent.EXTRA_TEXT, shareText)
//         type = "text/plain"
//     }
//     
//     context.startActivity(Intent.createChooser(shareIntent, "Compartilhar"))
// }

/**
 * EXEMPLO 11: Adicionar Voice Input
 */

// Permission no Manifest:
// <uses-permission android:name="android.permission.RECORD_AUDIO" />
//
// ViewModel:
// fun startVoiceInput() {
//     val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//         putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
//         putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga o título da tarefa...")
//     }
//     
//     voiceInputLauncher.launch(intent)
// }
//
// Handling resultado:
// val voiceInputLauncher = registerForActivityResult(
//     ActivityResultContracts.StartActivityForResult()
// ) { result ->
//     if (result.resultCode == Activity.RESULT_OK) {
//         val matches = result.data?.getStringArrayListExtra(
//             RecognizerIntent.EXTRA_RESULTS
//         )
//         matches?.let { 
//             val title = it.firstOrNull() ?: ""
//             addTodo(title)
//         }
//     }
// }

/**
 * EXEMPLO 12: Adicionar Export para PDF/CSV
 */

// Função para gerar CSV:
// fun exportTodosToCSV(): String {
//     val header = "ID,Título,Descrição,Concluída,Data\n"
//     val rows = todos.value.joinToString("\n") { todo ->
//         "${todo.id},${todo.title},${todo.description},${todo.isCompleted},${formatDate(todo.createdAt)}"
//     }
//     return header + rows
// }
//
// Função para gerar PDF (usar iText ou PDFKit):
// fun exportTodosToPDF() {
//     val doc = Document()
//     val writer = PdfWriter.getInstance(doc, FileOutputStream(filePath))
//     doc.open()
//     
//     todos.value.forEach { todo ->
//         val paragraph = Paragraph("${todo.title}\n${todo.description}")
//         doc.add(paragraph)
//     }
//     
//     doc.close()
// }

println("Exemplos práticos adicionais fornecidos!")
println("Descomente e integre conforme necessário ao seu projeto.")
