package com.tapiceria.app.di

import android.content.Context
import androidx.room.Room
import com.tapiceria.app.data.local.DatabaseMigrations
import com.tapiceria.app.data.local.TapiceriaDatabase
import com.tapiceria.app.data.repository.AtencionRepositoryImpl
import com.tapiceria.app.data.repository.ClienteRepositoryImpl
import com.tapiceria.app.data.repository.CotizacionRepositoryImpl
import com.tapiceria.app.data.repository.DashboardRepositoryImpl
import com.tapiceria.app.data.repository.FotoTrabajoRepositoryImpl
import com.tapiceria.app.data.repository.HistorialClienteRepositoryImpl
import com.tapiceria.app.data.repository.PagoRepositoryImpl
import com.tapiceria.app.domain.repository.AtencionRepository
import com.tapiceria.app.domain.repository.ClienteRepository
import com.tapiceria.app.domain.repository.CotizacionRepository

import com.tapiceria.app.data.repository.TrabajoRepositoryImpl
import com.tapiceria.app.domain.repository.DashboardRepository
import com.tapiceria.app.domain.repository.FotoTrabajoRepository
import com.tapiceria.app.domain.repository.PagoRepository
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
        )
            .addMigrations(
                DatabaseMigrations.MIGRATION_1_2
            )
            .build()

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

    // Repositorio para administrar las fotografías de los trabajos.
    val fotoTrabajoRepository: FotoTrabajoRepository =
        FotoTrabajoRepositoryImpl(database.fotoTrabajoDao())

    /**
     * Repositorio de pagos asociado a la base de datos local.
     */
    val pagoRepository: PagoRepository =
        PagoRepositoryImpl(database.pagoDao())

    /**
     * Repositorio encargado de los indicadores y listados del Dashboard.
     */
    val dashboardRepository: DashboardRepository =
        DashboardRepositoryImpl(database.dashboardDao())

    // Repositorio de consultas del historial de clientes.
    val historialClienteRepository =
        HistorialClienteRepositoryImpl(
            database.historialClienteDao()
        )
}