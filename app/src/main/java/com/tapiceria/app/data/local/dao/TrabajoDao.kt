
package com.tapiceria.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tapiceria.app.data.local.entity.TrabajoEntity
import com.tapiceria.app.domain.model.CotizacionTrabajoOpcion
import com.tapiceria.app.domain.model.TrabajoListado
import kotlinx.coroutines.flow.Flow

@Dao
interface TrabajoDao {

    @Insert
    suspend fun insertar(trabajo: TrabajoEntity): Long

    @Update
    suspend fun actualizar(trabajo: TrabajoEntity)

    @Query("SELECT * FROM trabajos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): TrabajoEntity?

    /**
     * Devuelve los trabajos con el nombre del cliente.
     */
    @Query("""
        SELECT
            t.id AS id,
            t.clienteId AS clienteId,
            t.cotizacionId AS cotizacionId,
            t.folio AS folio,
            c.nombre AS nombreCliente,
            t.descripcion AS descripcion,
            t.importeCentavos AS importeCentavos,
            t.fechaRecepcion AS fechaRecepcion,
            t.fechaEntregaEstimada AS fechaEntregaEstimada,
            t.fechaEntregaReal AS fechaEntregaReal,
            t.estado AS estado,
            t.notas AS notas
        FROM trabajos t
        INNER JOIN clientes c ON c.id = t.clienteId
        ORDER BY t.fechaRecepcion DESC
    """)
    fun observarTodos(): Flow<List<TrabajoListado>>

    /**
     * Obtiene cotizaciones aceptadas para convertirlas en trabajos.
     */
    @Query("""
        SELECT
            co.id AS id,
            a.clienteId AS clienteId,
            cl.nombre AS nombreCliente,
            co.folio AS folio,
            co.descripcion AS descripcion,
            co.importeCentavos AS importeCentavos
        FROM cotizaciones co
        INNER JOIN atenciones a ON a.id = co.atencionId
        INNER JOIN clientes cl ON cl.id = a.clienteId
        WHERE co.estado = 'ACEPTADA'
        ORDER BY co.fechaCreacion DESC
    """)
    fun observarCotizacionesAceptadas():
            Flow<List<CotizacionTrabajoOpcion>>

    /**
     * Permite actualizar el estado sin modificar los demás datos.
     */
    @Query("UPDATE trabajos SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Long, estado: String): Int
}