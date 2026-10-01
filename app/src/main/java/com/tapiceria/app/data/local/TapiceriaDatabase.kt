package com.tapiceria.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tapiceria.app.data.local.dao.ClienteDao
import com.tapiceria.app.data.local.entity.AtencionEntity
import com.tapiceria.app.data.local.entity.ClienteEntity
import com.tapiceria.app.data.local.entity.CotizacionEntity
import com.tapiceria.app.data.local.entity.FotoTrabajoEntity
import com.tapiceria.app.data.local.entity.PagoEntity
import com.tapiceria.app.data.local.entity.ServicioEntity
import com.tapiceria.app.data.local.entity.TrabajoEntity

// Agrega estos imports:
import com.tapiceria.app.data.local.dao.AtencionDao
import com.tapiceria.app.data.local.dao.CotizacionDao
import com.tapiceria.app.data.local.dao.DashboardDao
import com.tapiceria.app.data.local.dao.FotoTrabajoDao
import com.tapiceria.app.data.local.dao.HistorialClienteDao
import com.tapiceria.app.data.local.dao.PagoDao

import com.tapiceria.app.data.local.dao.TrabajoDao

/**
 * Punto de acceso a la base de datos local.
 *
 * La instancia se creará desde el contenedor de dependencias
 * de la aplicación; no se crea una instancia por pantalla.
 */
@Database(
    entities = [
        ClienteEntity::class,
        AtencionEntity::class,
        CotizacionEntity::class,
        TrabajoEntity::class,
        FotoTrabajoEntity::class,
        PagoEntity::class,
        ServicioEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class TapiceriaDatabase : RoomDatabase() {

    /**
     * Expone el DAO de clientes.
     * Agregaremos los demás DAO conforme implementemos sus módulos.
     */
    abstract fun clienteDao(): ClienteDao

    /** DAO para registrar y consultar atenciones. */
    abstract fun atencionDao(): AtencionDao

    /** DAO para administrar cotizaciones. */
    abstract fun cotizacionDao(): CotizacionDao

    // Expone las operaciones de acceso a trabajos.
    abstract fun trabajoDao(): TrabajoDao

    // Expone las operaciones de acceso a las fotografías.
    abstract fun fotoTrabajoDao(): FotoTrabajoDao

    /**
     * Proporciona acceso a las operaciones de pagos.
     */
    abstract fun pagoDao(): PagoDao

    /**
     * Proporciona acceso a las consultas del Dashboard.
     */
    abstract fun dashboardDao(): DashboardDao

    // DAO de consultas para el historial del cliente.
    abstract fun historialClienteDao(): HistorialClienteDao
}