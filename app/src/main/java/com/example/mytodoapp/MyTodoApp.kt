// com/example/mytodoapp/MyTodoApp.kt
package com.example.mytodoapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyTodoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar Hilt e outras dependências aqui

    }
}
