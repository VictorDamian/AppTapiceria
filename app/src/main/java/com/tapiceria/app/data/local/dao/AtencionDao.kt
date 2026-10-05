
package com.tapiceria.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tapiceria.app.data.local.entity.AtencionEntity
import com.tapiceria.app.domain.model.AtencionListado
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a datos de las atenciones de los clientes.
 */
@Dao
interface AtencionDao {

    /** Registra una consulta o solicitud de cotización. */
    @Insert
    suspend fun insertar(atencion: AtencionEntity): Long

    @Update
    suspend fun actualizar(atencion: AtencionEntity)

    /** Consulta una atención por su identificador. */
    @Query("SELECT * FROM atenciones WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): AtencionEntity?

    /**
     * Lista las atenciones con el nombre del cliente.
     * El nombre se obtiene mediante JOIN, sin duplicar información.
     * LEFT JOIN es necesario porque clienteId puede ser NULL.
     */
    @Query("""
        SELECT
            a.id AS id,
            a.clienteId AS clienteId,
            COALESCE(c.nombre, 'Pendiente') AS nombreCliente,
            a.tipo AS tipo,
            a.descripcion AS descripcion,
            a.fechaAtencion AS fechaAtencion,
            a.notas AS notas
        FROM atenciones a
        LEFT JOIN clientes c ON c.id = a.clienteId
        ORDER BY a.fechaAtencion DESC
    """)
    fun observarTodas(): Flow<List<AtencionListado>>

    /** Permite filtrar por tipo de atención.
     * Filtra las atenciones por tipo.
     *      *
     *      * También utiliza LEFT JOIN para conservar las atenciones
     *      * que no tienen cliente.
     *      */
    @Query("""
        SELECT
            a.id AS id,
            a.clienteId AS clienteId,
            COALESCE(c.nombre, 'Pendiente') AS nombreCliente,
            a.tipo AS tipo,
            a.descripcion AS descripcion,
            a.fechaAtencion AS fechaAtencion,
            a.notas AS notas
        FROM atenciones a
        LEFT JOIN clientes c ON c.id = a.clienteId
        WHERE a.tipo = :tipo
        ORDER BY a.fechaAtencion DESC
    """)
    fun observarPorTipo(tipo: String): Flow<List<AtencionListado>>
}