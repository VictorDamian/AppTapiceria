package com.tapiceria.app

import android.app.Application
import com.tapiceria.app.di.AppContainer

/**
 * Clase Application: se inicializa una sola vez por proceso.
 * Aquí se crean las dependencias compartidas de la aplicación.
 */
class TapiceriaApplication : Application() {

    // Acceso centralizado a la base de datos y los repositorios.
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        // Usa el contexto de aplicación para evitar fugas de memoria.
        container = AppContainer(applicationContext)
    }
}