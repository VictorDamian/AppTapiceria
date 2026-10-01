package com.tapiceria.app

import android.app.Application
import android.util.Log
import com.tapiceria.app.data.respaldo.RestauracionPendiente
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

        // La base de datos se restaura antes de que Room abra sus conexiones.
        try {
            RestauracionPendiente.aplicarSiExiste(this)
        } catch (ex: Exception) {
            // Si la restauración falla, se registra el error para diagnóstico.
            // El servicio intenta recuperar los archivos anteriores.
            Log.e(
                "TapiceriaApplication",
                "No se pudo aplicar el respaldo pendiente.",
                ex
            )
        }

        // Usa el contexto de aplicación para evitar fugas de memoria.
        container = AppContainer(applicationContext)
    }
}