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

    @Query("""
        SELECT 
        t.id,
        t.clienteId,
        t.estado,
        t.descripcion,
        t.fechaEntregaEstimada,
        t.fechaEntregaReal,
        t.fechaRecepcion,
        t.folio,
        t.notas,
        c.importeCentavos,
        t.cotizacionId
        FROM trabajos t
        INNER JOIN cotizaciones c 
        ON t.cotizacionId = c.id
        WHERE t.id = :id
        LIMIT 1
    """)
    suspend fun obtenerPorId(id: Long): TrabajoEntity?

    /**
     * Busca el trabajo activo que utiliza una cotización.
     *
     * CANCELADO libera la cotización para que pueda volver
     * a utilizarse en el flujo del negocio.
     */
    @Query("""
        SELECT *
        FROM trabajos
        WHERE cotizacionId = :cotizacionId
          AND estado <> 'CANCELADO'
        LIMIT 1
    """)
    suspend fun obtenerActivoPorCotizacion(
        cotizacionId: Long
    ): TrabajoEntity?

    /**
     * Devuelve todos los trabajos para historial y seguimiento.
     */
    @Query("""
        SELECT
            t.id AS id,
            t.clienteId AS clienteId,
            t.cotizacionId AS cotizacionId,
            t.folio AS folio,
            c.nombre AS nombreCliente,
            t.descripcion AS descripcion,
            co.importeCentavos AS importeCentavos,
            t.fechaRecepcion AS fechaRecepcion,
            t.fechaEntregaEstimada AS fechaEntregaEstimada,
            t.fechaEntregaReal AS fechaEntregaReal,
            t.estado AS estado,
            t.notas AS notas
        FROM trabajos t
        INNER JOIN clientes c
            ON c.id = t.clienteId
        INNER JOIN cotizaciones co 
            ON co.id = t.cotizacionId
        ORDER BY t.fechaRecepcion DESC
    """)
    fun observarTodos(): Flow<List<TrabajoListado>>

    /**
     * Obtiene cotizaciones que pueden utilizarse en un trabajo.
     *
     * Normalmente deben estar ACEPTADAS.
     *
     * También incluimos la cotización de un trabajo CANCELADO
     * para que ese trabajo pueda seguir editándose.
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
        INNER JOIN atenciones a
            ON a.id = co.atencionId
        INNER JOIN clientes cl
            ON cl.id = a.clienteId
        WHERE co.estado = 'ACEPTADA'
           OR EXISTS (
                SELECT 1
                FROM trabajos t
                WHERE t.cotizacionId = co.id
                  AND t.estado = 'CANCELADO'
           )
        ORDER BY co.fechaCreacion DESC
    """)
    fun observarCotizacionesAceptadas():
            Flow<List<CotizacionTrabajoOpcion>>

    /**
     * Actualiza únicamente el estado del trabajo.
     */
    @Query("""
        UPDATE trabajos
        SET estado = :estado
        WHERE id = :id
    """)
    suspend fun actualizarEstado(
        id: Long,
        estado: String
    ): Int
}