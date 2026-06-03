// GUIA: Como Integrar API ao Projeto
// com/example/mytodoapp/data/remote/TodoRemoteDataSource.kt

package com.example.mytodoapp.data.remote

import com.example.mytodoapp.data.model.ApiResponse
import com.example.mytodoapp.data.model.CreateTodoRequest
import com.example.mytodoapp.data.model.Todo
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PASSO 1: Definir a API Interface
 */
interface TodoApiService {
    
    @GET("todos")
    suspend fun getAllTodos(): ApiResponse<List<Todo>>
    
    @GET("todos/{id}")
    suspend fun getTodoById(@Path("id") id: Int): ApiResponse<Todo>
    
    @POST("todos")
    suspend fun createTodo(@Body request: CreateTodoRequest): ApiResponse<Todo>
    
    @PUT("todos/{id}")
    suspend fun updateTodo(
        @Path("id") id: Int,
        @Body request: CreateTodoRequest
    ): ApiResponse<Todo>
    
    @DELETE("todos/{id}")
    suspend fun deleteTodo(@Path("id") id: Int): ApiResponse<Unit>
}

/**
 * PASSO 2: Criar o Remote Data Source
 */
@Singleton
class TodoRemoteDataSource @Inject constructor(
    private val apiService: TodoApiService
) {
    
    suspend fun getAllTodos(): List<Todo> {
        try {
            val response = apiService.getAllTodos()
            if (response.success && response.data != null) {
                return response.data
            }
            throw Exception(response.message ?: "Erro desconhecido")
        } catch (e: Exception) {
            throw Exception("Erro ao buscar todos: ${e.message}")
        }
    }
    
    suspend fun getTodoById(id: Int): Todo? {
        return try {
            val response = apiService.getTodoById(id)
            if (response.success) response.data else null
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun createTodo(title: String, description: String): Todo {
        try {
            val request = CreateTodoRequest(title, description)
            val response = apiService.createTodo(request)
            if (response.success && response.data != null) {
                return response.data
            }
            throw Exception(response.message ?: "Erro ao criar tarefa")
        } catch (e: Exception) {
            throw Exception("Erro ao criar tarefa: ${e.message}")
        }
    }
    
    suspend fun updateTodo(id: Int, title: String, description: String): Todo {
        try {
            val request = CreateTodoRequest(title, description)
            val response = apiService.updateTodo(id, request)
            if (response.success && response.data != null) {
                return response.data
            }
            throw Exception(response.message ?: "Erro ao atualizar tarefa")
        } catch (e: Exception) {
            throw Exception("Erro ao atualizar tarefa: ${e.message}")
        }
    }
    
    suspend fun deleteTodo(id: Int) {
        try {
            val response = apiService.deleteTodo(id)
            if (!response.success) {
                throw Exception(response.message ?: "Erro ao deletar tarefa")
            }
        } catch (e: Exception) {
            throw Exception("Erro ao deletar tarefa: ${e.message}")
        }
    }
}

/**
 * PASSO 3: Adicionar ao AppModule (di/AppModule.kt)
 * 
 * // Criar módulo separado
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object NetworkModule {
 *     
 *     @Singleton
 *     @Provides
 *     fun provideHttpClient(): OkHttpClient {
 *         val logging = HttpLoggingInterceptor().apply {
 *             level = HttpLoggingInterceptor.Level.BODY
 *         }
 *         return OkHttpClient.Builder()
 *             .addInterceptor(logging)
 *             .build()
 *     }
 *     
 *     @Singleton
 *     @Provides
 *     fun provideRetrofit(httpClient: OkHttpClient): Retrofit {
 *         val contentType = "application/json".toMediaType()
 *         val json = Json { ignoreUnknownKeys = true }
 *         
 *         return Retrofit.Builder()
 *             .baseUrl("https://seu-api.com/api/")
 *             .client(httpClient)
 *             .addConverterFactory(json.asConverterFactory(contentType))
 *             .build()
 *     }
 *     
 *     @Singleton
 *     @Provides
 *     fun provideTodoApiService(retrofit: Retrofit): TodoApiService {
 *         return retrofit.create(TodoApiService::class.java)
 *     }
 * }
 */

/**
 * PASSO 4: Atualizar o Repository (data/repository/TodoRepository.kt)
 * 
 * class TodoRepository @Inject constructor(
 *     private val todoDao: TodoDao,
 *     private val remoteDataSource: TodoRemoteDataSource
 * ) : ITodoRepository {
 *     
 *     override fun getAllTodos(): Flow<List<Todo>> {
 *         // Opção 1: Cache first, depois network
 *         return flow {
 *             // Emitir cache primeiro
 *             emitAll(todoDao.getAllTodos())
 *             
 *             // Depois buscar do servidor
 *             try {
 *                 val remoteTodos = remoteDataSource.getAllTodos()
 *                 todoDao.clearAllTodos()
 *                 remoteTodos.forEach { todoDao.insertTodo(it) }
 *             } catch (e: Exception) {
 *                 // Erro na sincronização, mas cache já foi mostrado
 *             }
 *         }
 *     }
 *     
 *     override suspend fun addTodo(todo: Todo): Long {
 *         // Criar localmente
 *         val localId = todoDao.insertTodo(todo)
 *         
 *         // Tentar sincronizar com servidor
 *         return try {
 *             val remoteTodo = remoteDataSource.createTodo(todo.title, todo.description)
 *             // Atualizar com ID do servidor
 *             val updatedTodo = todo.copy(id = remoteTodo.id)
 *             todoDao.updateTodo(updatedTodo)
 *             remoteTodo.id.toLong()
 *         } catch (e: Exception) {
 *             // Manter local se falhar
 *             localId
 *         }
 *     }
 * }
 */

/**
 * PASSO 5: Estratégias de Sincronização
 * 
 * A) CACHE FIRST (recomendado para apps offline-first):
 *    - Mostrar dados do cache primeiro
 *    - Sincronizar com servidor em background
 *    - Usar Room como fonte de verdade
 * 
 * B) NETWORK FIRST:
 *    - Tentar buscar do servidor
 *    - Se falhar, usar cache
 *    - Mais lento mas sempre atualizado
 * 
 * C) NETWORK ONLY:
 *    - Sempre buscar do servidor
 *    - Mais simples, sem sincronização complexa
 *    - Não funciona offline
 * 
 * Exemplo: Com Hilt, você pode injetar um Repository que escolhe a estratégia
 * com base em conectividade da rede.
 */
