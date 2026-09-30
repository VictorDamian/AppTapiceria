package com.tapiceria.app.di

import android.content.Context
import androidx.room.Room
import com.tapiceria.app.data.local.TapiceriaDatabase
import com.tapiceria.app.data.repository.ClienteRepositoryImpl
import com.tapiceria.app.domain.repository.ClienteRepository

/**
 * Contenedor de dependencias de la aplicación.
 *
 * Mantiene una única instancia de la base de datos y del
 * repositorio durante la ejecución del proceso de la app.
 */
class AppContainer(context: Context) {

    // Conserva el contexto de aplicación para evitar fugas de memoria.
    private val database: TapiceriaDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            TapiceriaDatabase::class.java,
            "tapiceria_database"
        ).build()

    /** Repositorio que utilizarán las pantallas y ViewModels. */
    val clienteRepository: ClienteRepository =
        ClienteRepositoryImpl(database.clienteDao())
}