# Guia de Migração: XML/Fragments → Jetpack Compose

Para desenvolvedores com 6+ anos de experiência migrando para Compose.

## 📊 Comparação Rápida

### Layout XML
```xml
<!-- res/layout/todo_item.xml -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="16dp">
    
    <CheckBox
        android:id="@+id/checkBox"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
    
    <TextView
        android:id="@+id/titleText"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1" />
    
    <ImageButton
        android:id="@+id/deleteBtn"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
</LinearLayout>

<!-- MainActivity -->
<activity
    android:name=".MainActivity"
    android:label="@string/app_name">
    ...
</activity>
```

### Jetpack Compose
```kotlin
// Único arquivo .kt
@Composable
fun TodoItem(
    todo: Todo,
    onDelete: (Todo) -> Unit,
    onToggleCompletion: (Todo) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = "Toggle",
            modifier = Modifier.clickable { 
                onToggleCompletion(todo) 
            }
        )
        
        Text(
            todo.title,
            modifier = Modifier.weight(1f)
        )
        
        Icon(
            Icons.Default.Delete,
            contentDescription = "Delete",
            modifier = Modifier.clickable { onDelete(todo) }
        )
    }
}

// MainActivity
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoScreen()
        }
    }
}
```

## 🔄 Mapeamento de Conceitos

| XML + Fragment | Jetpack Compose |
|---|---|
| Fragment | @Composable função |
| ViewModel | ViewModel (igual) |
| LiveData | StateFlow / Flow |
| RecyclerView + Adapter | LazyColumn + items() |
| ViewBinding | state management via Composable |
| Fragment navigation | Navigation Compose |
| SharedPreferences | DataStore |
| AlertDialog | AlertDialog composable |

## 📋 Checklist de Migração

### Para cada Activity/Fragment:

- [ ] Converter layout XML em Composable
- [ ] Adaptar ViewModel para usar StateFlow
- [ ] Remover findViewById (viewBinding)
- [ ] Adaptar RecyclerView para LazyColumn
- [ ] Converter dialogs para Compose dialogs
- [ ] Testar states e transitions
- [ ] Validar dark mode

## 🎯 Padrões Comuns

### 1. RecyclerView → LazyColumn

**Antes:**
```kotlin
class TodoAdapter : RecyclerView.Adapter<TodoViewHolder>() {
    override fun onCreateViewHolder(...) = TodoViewHolder(...)
    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(todos[position])
    }
    override fun getItemCount() = todos.size
}

// Em Fragment
val adapter = TodoAdapter(todos)
recyclerView.adapter = adapter
```

**Depois:**
```kotlin
LazyColumn {
    items(todos, key = { it.id }) { todo ->
        TodoItem(todo)
    }
}
```

### 2. LiveData → StateFlow

**Antes:**
```kotlin
class TodoViewModel : ViewModel() {
    private val _todos = MutableLiveData<List<Todo>>()
    val todos: LiveData<List<Todo>> = _todos
    
    fun loadTodos() {
        _todos.value = repository.getTodos()
    }
}

// Em Fragment
viewModel.todos.observe(viewLifecycleOwner) { todos ->
    adapter.submitList(todos)
}
```

**Depois:**
```kotlin
@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {
    val todos: StateFlow<List<Todo>> = repository.getAllTodos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

// Em Composable
val todos by viewModel.todos.collectAsState()
LazyColumn {
    items(todos) { todo -> TodoItem(todo) }
}
```

### 3. AlertDialog → Composable Dialog

**Antes:**
```kotlin
AlertDialog.Builder(context)
    .setTitle("Deletar?")
    .setMessage("Deseja deletar esta tarefa?")
    .setPositiveButton("Sim") { _, _ -> 
        viewModel.deleteTodo(todo)
    }
    .setNegativeButton("Não", null)
    .show()
```

**Depois:**
```kotlin
@Composable
fun DeleteConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deletar?") },
        text = { Text("Deseja deletar esta tarefa?") },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text("Sim")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Não")
            }
        }
    )
}
```

### 4. Fragment Navigation → Navigation Compose

**Antes:**
```xml
<!-- navigation/nav_graph.xml -->
<navigation>
    <fragment
        android:id="@+id/todoFragment"
        android:name=".ui.TodoFragment" />
    <fragment
        android:id="@+id/detailFragment"
        android:name=".ui.DetailFragment" />
</navigation>
```

```kotlin
// Em Fragment
findNavController().navigate(
    TodoFragmentDirections.actionTodoToDetail(todoId)
)
```

**Depois:**
```kotlin
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "todos"
    ) {
        composable("todos") {
            TodoScreen(
                onTodoClick = { todoId ->
                    navController.navigate("detail/$todoId")
                }
            )
        }
        
        composable(
            "detail/{todoId}",
            arguments = listOf(navArgument("todoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val todoId = backStackEntry.arguments?.getInt("todoId") ?: return@composable
            DetailScreen(todoId)
        }
    }
}
```

### 5. SharedPreferences → DataStore

**Antes:**
```kotlin
val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
val isDarkMode = prefs.getBoolean("dark_mode", false)

prefs.edit().putBoolean("dark_mode", true).apply()
```

**Depois:**
```kotlin
// datastore.proto
syntax = "proto3";

message AppSettings {
    bool dark_mode = 1;
}

// DataStore Repository
class SettingsRepository(private val dataStore: DataStore<AppSettings>) {
    val darkModeFlow: Flow<Boolean> =
        dataStore.data.map { it.darkMode }
    
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.updateData { 
            it.copy(darkMode = enabled)
        }
    }
}
```

## 🎨 State Management Patterns

### Pattern 1: State Hoisting

```kotlin
// ❌ Errado: State interno
@Composable
fun TodoForm() {
    var title by remember { mutableStateOf("") }
    TextField(value = title, onValueChange = { title = it })
}

// ✅ Correto: State hoisted
@Composable
fun TodoForm(
    title: String,
    onTitleChange: (String) -> Unit
) {
    TextField(value = title, onValueChange = onTitleChange)
}

// Parent controla o estado
@Composable
fun TodoScreen(viewModel: TodoViewModel) {
    var title by remember { mutableStateOf("") }
    TodoForm(title = title, onTitleChange = { title = it })
}
```

### Pattern 2: Side Effects

```kotlin
@Composable
fun TodoScreen(viewModel: TodoViewModel) {
    val todos by viewModel.todos.collectAsState()
    
    // Efeito ao montar (substitui onCreate)
    LaunchedEffect(Unit) {
        viewModel.loadTodos()
    }
    
    // Efeito quando algo mudar
    LaunchedEffect(todos.size) {
        if (todos.isEmpty()) {
            showMessage("Nenhuma tarefa")
        }
    }
    
    // Efeito para cleanup (substitui onDestroy)
    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanup()
        }
    }
}
```

## 🧪 Diferenças em Testes

### ViewHolder Test → Composable Test

**Antes:**
```kotlin
@RunWith(AndroidJUnit4::class)
class TodoAdapterTest {
    @get:Rule val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun testTodoItemDisplay() {
        val activity = activityRule.scenario
        activity.onActivity { mainActivity ->
            val holder = TodoViewHolder(...)
            holder.bind(Todo(1, "Test"))
            
            assertEquals("Test", holder.titleView.text)
        }
    }
}
```

**Depois:**
```kotlin
class TodoItemTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testTodoItemDisplay() {
        composeTestRule.setContent {
            TodoItem(
                todo = Todo(1, "Test"),
                onDelete = {},
                onToggleCompletion = {}
            )
        }
        
        composeTestRule.onNodeWithText("Test").assertIsDisplayed()
    }
}
```

## 🚨 Armadilhas Comuns

### 1. Recomposição Desnecessária
```kotlin
// ❌ Ruim: Recalcula a cada recomposição
val expensiveList = todos.filter { it.isCompleted }.sortedBy { it.date }

// ✅ Bom: Memoiza resultado
val expensiveList = remember(todos) {
    todos.filter { it.isCompleted }.sortedBy { it.date }
}
```

### 2. Mutable State Sem Remember
```kotlin
// ❌ Ruim: Perde estado ao recompor
var count = 0

// ✅ Bom
var count by remember { mutableStateOf(0) }
```

### 3. Callbacks Instáveis
```kotlin
// ❌ Ruim: Nova função a cada recomposição
@Composable
fun TodoItem(todo: Todo) {
    Button(onClick = { deleteTodo(todo) }) {} // Novo callback!
}

// ✅ Bom: Callback estável
@Composable
fun TodoItem(
    todo: Todo,
    onDelete: (Todo) -> Unit = {}
) {
    Button(onClick = { onDelete(todo) }) {}
}
```

## 📈 Checklist de Performance

- [ ] Usar `LazyColumn` para listas grandes
- [ ] Memoizar computações caras
- [ ] Decompor composables grandes
- [ ] Evitar lambdas inline em modificadores
- [ ] Usar `remember` para preservar identidade
- [ ] Evitar recomposições desnecessárias

## 🎓 Ordem de Aprendizado Recomendada

1. **Semana 1**: Basics de Compose (Composables, Modifiers, State)
2. **Semana 2**: Layouts (Row, Column, LazyColumn)
3. **Semana 3**: State Management (ViewModel, StateFlow)
4. **Semana 4**: Navegação (Navigation Compose)
5. **Semana 5**: APIs (Retrofit com Compose)
6. **Semana 6**: Testes e Performance

## 📚 Referências Rápidas

```kotlin
// Dimensões
Spacer(modifier = Modifier.height(16.dp))

// Cores
Text(color = MaterialTheme.colorScheme.primary)

// Icons
Icon(Icons.Default.Add, contentDescription = "Add")

// Cliques
Button(onClick = { /* ... */ })

// Condições
if (isLoading) {
    CircularProgressIndicator()
} else {
    TodoList(todos)
}
```

---

**Bem-vindo ao futuro do desenvolvimento Android! 🚀**
