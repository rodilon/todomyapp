# Projeto Todo App com Jetpack Compose

Um projeto modularizado e atualizado que demonstra as melhores práticas de desenvolvimento Android com **Jetpack Compose**, **Room**, **Coroutines** e **Hilt**.

## 🎯 Objetivo

Este projeto foi criado para ajudar desenvolvedores Android com experiência anterior a se atualizarem com as ferramentas modernas, começando com **dados locais** e preparado para **integração com API** posteriormente.

## 📁 Estrutura do Projeto

```
MyTodoApp/
├── data/
│   ├── local/
│   │   ├── TodoDatabase.kt      # Room Database
│   │   └── TodoDao.kt            # Data Access Object
│   ├── model/
│   │   └── Todo.kt               # Data classes
│   ├── remote/
│   │   └── TodoRemoteDataSource.kt  # (Para adicionar depois)
│   └── repository/
│       └── TodoRepository.kt     # Abstração de dados
├── di/
│   └── AppModule.kt              # Dependency Injection
├── ui/
│   ├── screens/
│   │   └── TodoScreen.kt         # UI Principal
│   ├── viewmodel/
│   │   └── TodoViewModel.kt      # Lógica da UI
│   └── theme/
│       ├── Theme.kt
│       └── Typography.kt
├── MainActivity.kt
└── MyTodoApp.kt                  # Application class
```

## 🚀 Principais Ferramentas

### 1. **Jetpack Compose**
- UI declarativa e reativa
- Menos boilerplate que XML
- Preview em tempo real

### 2. **Room Database**
- Banco de dados local SQLite
- Type-safe queries
- Integrado com Coroutines via Flow

### 3. **ViewModel + StateFlow**
- Gerenciamento de estado
- Sobrevive a recomposição de configuração
- Reativo com Flow/StateFlow

### 4. **Hilt**
- Dependency Injection automático
- Reduz código boilerplate
- Facilita testes

### 5. **Coroutines**
- Operações assíncronas
- Flow para streams de dados
- Lançamento de scope automático

## 📚 Conceitos Principais

### Composables
São funções que descrevem a UI:

```kotlin
@Composable
fun TodoScreen() {
    Column {
        Text("Minhas Tarefas")
        // Mais composables aqui
    }
}
```

**Características:**
- Função anotada com `@Composable`
- Sem retorno (resultado é composição de UI)
- Recomposição automática quando estado muda
- Preview possível com `@Preview`

### StateFlow vs LiveData

| StateFlow | LiveData |
|-----------|----------|
| Mais moderno | Mais antigo |
| Funciona fora do ciclo de vida | Lifecycle-aware |
| Melhor com Coroutines | Bom com Android |
| Use para novos projetos | Legado |

### Repository Pattern

```
UI (Compose) 
    ↓
ViewModel
    ↓
Repository (abstrai origem dos dados)
    ↓
LocalDataSource (Room) ← RemoteDataSource (API)
```

Benefício: Trocar de fonte de dados sem mudar a UI.

### Diferenças com XML

**Antes (XML + Fragment):**
```xml
<!-- layout/fragment_todo.xml -->
<RecyclerView
    android:id="@+id/todoRecycler"
    android:layout_width="match_parent" />
```

**Agora (Compose):**
```kotlin
@Composable
fun TodoScreen() {
    LazyColumn {
        items(todos) { todo ->
            TodoItem(todo)
        }
    }
}
```

## 🔄 Fluxo de Dados

```
User Action (ex: click) 
    ↓
ViewModel (updateTodo())
    ↓
Repository (atualizar dados)
    ↓
Room (persistir)
    ↓
Flow<List<Todo>> emite novo valor
    ↓
TodoScreen recompõe automaticamente
```

## 🛠️ Como Usar

### 1. Adicionar uma Tarefa
```kotlin
// No ViewModel
fun addTodo(title: String, description: String) {
    viewModelScope.launch {
        repository.addTodo(Todo(title = title, description = description))
    }
}

// Na UI
Button(onClick = { viewModel.addTodo(title, description) }) {
    Text("Adicionar")
}
```

### 2. Observar Tarefas
```kotlin
// No ViewModel
val todos: StateFlow<List<Todo>> = repository.getAllTodos()
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

// Na Composable
val todos by viewModel.todos.collectAsState()
LazyColumn {
    items(todos) { todo -> ... }
}
```

## 📱 Adaptações para Diferentes Telas

```kotlin
@Composable
fun TodoScreen(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val isMobile = configuration.screenWidthDp < 600
    
    if (isMobile) {
        TodoScreenMobile()
    } else {
        TodoScreenTablet()
    }
}
```

## 🌐 Próximo Passo: Adicionar API

Quando quiser integrar uma API:

1. **Criar Retrofit Service** (ver arquivo `API_INTEGRATION_GUIDE.kt`)
2. **Implementar Remote Data Source**
3. **Adicionar Network Module no Hilt**
4. **Atualizar Repository com sincronização**
5. **Implementar tratamento de erros de rede**

Ver detalhes no arquivo: `com/example/mytodoapp/data/remote/TodoRemoteDataSource.kt`

## 🧪 Testes

### Unit Test (ViewModel)
```kotlin
@Test
fun testAddTodo() = runTest {
    val repository = FakeTodoRepository()
    val viewModel = TodoViewModel(repository)
    
    viewModel.addTodo("Test", "Description")
    
    assertEquals(1, viewModel.todos.value.size)
}
```

### Compose Test
```kotlin
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
```

## 📖 Recursos para Aprender

### Documentação Oficial
- **Jetpack Compose**: https://developer.android.com/compose
- **Room**: https://developer.android.com/jetpack/androidx/releases/room
- **Hilt**: https://developer.android.com/training/dependency-injection/hilt-android
- **Coroutines**: https://kotlinlang.org/docs/coroutines-overview.html

### Codelabs (Google)
- Jetpack Compose Basics: https://developer.android.com/codelabs/jetpack-compose-basics
- Advanced State in Compose: https://developer.android.com/codelabs/jetpack-compose-advanced-state-management

## 🎨 Material Design 3

Este projeto usa Material Design 3:
- Cores dinâmicas (Android 12+)
- Componentes atualizados
- Dark mode suportado

```kotlin
Text(
    "Hello",
    style = MaterialTheme.typography.headlineSmall,
    color = MaterialTheme.colorScheme.primary
)
```

## 🔒 Permissões

Para adicionar API, adicione ao `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 💡 Dicas Práticas

1. **State Hoisting**: Mova estado para componentes pai quando múltiplos componentes precisam
2. **Remember**: Use `remember { }` para estado mutável dentro da composição
3. **LaunchedEffect**: Use para efeitos colaterais (chamadas de API, etc)
4. **Lazy Layouts**: Use `LazyColumn`/`LazyRow` para listas grandes
5. **Preview**: Use `@Preview` para visualizar UI sem executar o app

## 🚀 Performance

- Use `produceState` para observar LiveData em Compose
- `remember` evita recálculos desnecessários
- Decomponha composables grandes em menores
- Use `key` em LazyColumn para identidades estáveis

## 📝 Changelog

- **v1.0**: Versão inicial com Compose, Room e Hilt
- **v2.0 (próximo)**: API remota com Retrofit
- **v3.0 (futuro)**: Sincronização offline-first

---

**Desenvolvido para demonstrar as melhores práticas modernas de Android em 2024**
