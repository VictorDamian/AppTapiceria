
package com.tapiceria.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tapiceria.app.data.local.entity.AtencionEntity
import com.tapiceria.app.data.local.entity.ClienteEntity
import com.tapiceria.app.data.local.entity.CotizacionEntity
import com.tapiceria.app.data.local.entity.FotoTrabajoEntity
import com.tapiceria.app.data.local.entity.PagoEntity
import com.tapiceria.app.data.local.entity.TrabajoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Consultas de solo lectura para mostrar el historial de un cliente.
 */
@Dao
interface HistorialClienteDao {

    // Información general del cliente.
    @Query("SELECT * FROM clientes WHERE id = :clienteId LIMIT 1")
    fun observarCliente(clienteId: Long): Flow<ClienteEntity?>

    // Consultas y cotizaciones se relacionan con el cliente mediante AtencionEntity.
    @Query(
        """
        SELECT * FROM atenciones
        WHERE clienteId = :clienteId
        ORDER BY fechaAtencion DESC
        """
    )
    fun observarAtenciones(clienteId: Long): Flow<List<AtencionEntity>>

    @Query(
        """
        SELECT cotizaciones.*
        FROM cotizaciones
        INNER JOIN atenciones
            ON atenciones.id = cotizaciones.atencionId
        WHERE atenciones.clienteId = :clienteId
        ORDER BY cotizaciones.fechaCreacion DESC
        """
    )
    fun observarCotizaciones(clienteId: Long): Flow<List<CotizacionEntity>>

    // Trabajos del cliente, incluidos los que se encuentran terminados o entregados.
    @Query(
        """
        SELECT * FROM trabajos
        WHERE clienteId = :clienteId
        ORDER BY fechaRecepcion DESC
        """
    )
    fun observarTrabajos(clienteId: Long): Flow<List<TrabajoEntity>>

    // Pagos asociados a cualquiera de los trabajos del cliente.
    @Query(
        """
        SELECT pagos.*
        FROM pagos
        INNER JOIN trabajos
            ON trabajos.id = pagos.trabajoId
        WHERE trabajos.clienteId = :clienteId
        ORDER BY pagos.fechaPago DESC
        """
    )
    fun observarPagos(clienteId: Long): Flow<List<PagoEntity>>

    // Fotografías asociadas a los trabajos del cliente.
    @Query(
        """
        SELECT fotos_trabajo.*
        FROM fotos_trabajo
        INNER JOIN trabajos
            ON trabajos.id = fotos_trabajo.trabajoId
        WHERE trabajos.clienteId = :clienteId
        ORDER BY fotos_trabajo.fechaRegistro DESC
        """
    )
    fun observarFotografias(clienteId: Long): Flow<List<FotoTrabajoEntity>>
}