package com.tapiceria.app.di

import android.content.Context
import androidx.room.Room
import com.tapiceria.app.data.local.TapiceriaDatabase
import com.tapiceria.app.data.repository.AtencionRepositoryImpl
import com.tapiceria.app.data.repository.ClienteRepositoryImpl
import com.tapiceria.app.data.repository.CotizacionRepositoryImpl
import com.tapiceria.app.domain.repository.AtencionRepository
import com.tapiceria.app.domain.repository.ClienteRepository
import com.tapiceria.app.domain.repository.CotizacionRepository

import com.tapiceria.app.data.repository.TrabajoRepositoryImpl
import com.tapiceria.app.domain.repository.TrabajoRepository
/**
 * Contenedor central de dependencias.
 * Se comparte durante la ejecución del proceso de la aplicación.
 */
class AppContainer(context: Context) {

    // Una única instancia de Room para toda la aplicación.
    private val database: TapiceriaDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            TapiceriaDatabase::class.java,
            "tapiceria_database"
        ).build()

    // Repositorios que consumirán los ViewModels.
    val clienteRepository: ClienteRepository =
        ClienteRepositoryImpl(database.clienteDao())

    val atencionRepository: AtencionRepository =
        AtencionRepositoryImpl(database.atencionDao())

    val cotizacionRepository: CotizacionRepository =
        CotizacionRepositoryImpl(database.cotizacionDao())

    // Repositorio para registrar y consultar trabajos.
    val trabajoRepository: TrabajoRepository =
        TrabajoRepositoryImpl(database.trabajoDao())
}