package com.tapiceria.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tapiceria.app.data.local.entity.CotizacionEntity
import com.tapiceria.app.domain.model.CotizacionListado
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a datos de cotizaciones.
 */
@Dao
interface CotizacionDao {

    /**
     * Inserta una cotización.
     */
    @Insert
    suspend fun insertar(
        cotizacion: CotizacionEntity
    ): Long

    /**
     * Actualiza una cotización existente.
     */
    @Update
    suspend fun actualizar(
        cotizacion: CotizacionEntity
    )

    /**
     * Obtiene una cotización por ID.
     */
    @Query("""
        SELECT *
        FROM cotizaciones
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun obtenerPorId(
        id: Long
    ): CotizacionEntity?

    /**
     * Obtiene la cotización asociada a una atención.
     *
     * Como la regla de negocio indica que solamente puede existir
     * una cotización por atención, esta consulta devuelve una sola.
     */
    @Query("""
        SELECT *
        FROM cotizaciones
        WHERE atencionId = :atencionId
        LIMIT 1
    """)
    suspend fun obtenerPorAtencion(
        atencionId: Long
    ): CotizacionEntity?

    /**
     * Obtiene las cotizaciones de una atención.
     */
    @Query("""
        SELECT *
        FROM cotizaciones
        WHERE atencionId = :atencionId
        ORDER BY fechaCreacion DESC
    """)
    fun observarPorAtencion(
        atencionId: Long
    ): Flow<List<CotizacionEntity>>

    /**
     * Obtiene cotizaciones por estado.
     */
    @Query("""
        SELECT *
        FROM cotizaciones
        WHERE estado = :estado
        ORDER BY fechaCreacion DESC
    """)
    fun observarPorEstado(
        estado: String
    ): Flow<List<CotizacionEntity>>

    /**
     * Obtiene las cotizaciones con la información necesaria
     * para mostrar el listado.
     */
    @Query("""
        SELECT
            co.id AS id,
            co.atencionId AS atencionId,
            co.folio AS folio,
            cl.nombre AS nombreCliente,
            a.descripcion AS descripcionAtencion,
            co.descripcion AS descripcionCotizacion,
            co.importeCentavos AS importeCentavos,
            co.fechaCreacion AS fechaCreacion,
            co.fechaVigencia AS fechaVigencia,
            co.estado AS estado
        FROM cotizaciones co
        INNER JOIN atenciones a
            ON a.id = co.atencionId
        INNER JOIN clientes cl
            ON cl.id = a.clienteId
        ORDER BY co.fechaCreacion DESC
    """)
    fun observarTodas(): Flow<List<CotizacionListado>>

    /**
     * Marca como vencidas las cotizaciones pendientes
     * cuya fecha de vigencia ya terminó.
     */
    @Query("""
        UPDATE cotizaciones
        SET estado = 'VENCIDA'
        WHERE estado = 'PENDIENTE'
          AND fechaVigencia IS NOT NULL
          AND fechaVigencia < :ahora
    """)
    suspend fun marcarVencidas(
        ahora: Long
    ): Int
}