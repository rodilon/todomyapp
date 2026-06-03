# MyTodoApp - Android Modern Architecture

Este projeto é uma aplicação de lista de tarefas (To-Do List) desenvolvida com as ferramentas e práticas mais modernas do ecossistema Android em 2026. Ele serve como um guia prático para desenvolvedores que buscam se atualizar com **Jetpack Compose**, **Clean Architecture**, **Hilt** e **Room**.

## 🎯 Objetivo

Demonstrar a implementação de um fluxo de dados robusto, desde a persistência local até a interface reativa, preparando o terreno para futuras integrações com APIs remotas.

## 📁 Estrutura do Projeto

O projeto segue uma estrutura organizada por camadas, facilitando a manutenção e os testes:

```text
com.example.mytodoapp/
├── data/
│   ├── local/
│   │   ├── TodoDatabase.kt      # Configuração do Room Database
│   │   └── TodoDao.kt            # Interface de acesso ao SQLite
│   ├── model/
│   │   └── Todo.kt               # Entidade de dados (Data Class)
│   └── repository/
│       ├── ITodoRepository.kt    # Interface do Repositório (Abstração)
│       └── TodoRepository.kt     # Implementação da lógica de dados
├── di/
│   └── AppModule.kt              # Injeção de Dependências com Hilt
├── ui/
│   ├── screens/
│   │   └── TodoScreen.kt         # UI declarativa com Jetpack Compose
│   ├── viewmodel/
│   │   └── TodoViewModel.kt      # Gerenciamento de estado e lógica de negócio
│   └── theme/
│       ├── Theme.kt              # Definição de tema Material 3
│       └── Typography.kt         # Configurações de tipografia
├── MainActivity.kt               # Ponto de entrada da UI
└── MyTodoApp.kt                  # Classe Application (Inicializa o Hilt)
```

## 🚀 Funcionalidades Principais

### 1. **Gestão de Tarefas**
- Adição, exclusão e marcação de conclusão de tarefas.
- Persistência local automática com Room.
- Filtros por estado (Todas, Ativas, Concluídas).
- Pesquisa em tempo real.

### 2. **Sistema de Snackbar & Undo (Novo!)**
- Feedback visual instantâneo para ações de Cadastro, Exclusão e Atualização.
- **Sistema de Reversão (Undo)**: Ao excluir ou alterar uma tarefa, o usuário tem a opção de "Desfazer" a ação diretamente pelo Snackbar.
- Implementado via `UiEvent` e `LaunchedEffect` para garantir que as mensagens apareçam de forma consistente com o ciclo de vida do Compose.

### 3. **Arquitetura Reativa**
- **StateFlow**: O estado da UI é exposto como um fluxo contínuo de dados.
- **Unidirectional Data Flow (UDF)**: Os eventos fluem da UI para o ViewModel, e o estado flui do ViewModel para a UI.

## 🛠️ Tecnologias Utilizadas

- **Jetpack Compose**: UI moderna e declarativa.
- **Hilt (Dagger)**: Injeção de dependência simplificada.
- **Room**: Abstração de banco de dados SQLite com suporte a Flow.
- **Coroutines & Flow**: Programação assíncrona e fluxos de dados reativos.
- **Material 3**: Design system mais recente do Google.

## 🔄 Fluxo de Trabalho do "Desfazer" (Undo)

1. **Ação**: O usuário deleta uma tarefa (ex: via Swipe-to-Dismiss).
2. **Evento**: O `TodoViewModel` remove o item do banco e dispara um `UiEvent.TodoDeleted` contendo os dados da tarefa removida.
3. **Feedback**: O `TodoScreen` captura o evento e exibe um Snackbar com a mensagem "Tarefa deletada!" e o botão "Desfazer".
4. **Reversão**: Se o usuário clicar em "Desfazer", o ViewModel executa `undoDelete(todo)`, reinserindo os dados originais no banco.

## 🛠️ Como Executar

1. Clone o repositório.
2. Abra no Android Studio (versão Ladybug ou superior recomendada).
3. Sincronize o Gradle.
4. Execute no emulador ou dispositivo físico.

## 🌐 Próximos Passos
- Integração com API REST usando **Retrofit**.
- Sincronização offline-first.
- Notificações locais para lembretes.

---
*Desenvolvido como um exemplo prático de arquitetura moderna em Android.*
