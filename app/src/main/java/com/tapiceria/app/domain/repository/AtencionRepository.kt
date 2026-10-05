
package com.tapiceria.app.domain.repository

import com.tapiceria.app.data.local.entity.AtencionEntity
import com.tapiceria.app.domain.model.AtencionListado
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para gestionar consultas y solicitudes de cotización.
 */
interface AtencionRepository {

    /**
     * Inserta una nueva atención.
     */
    suspend fun insertar(atencion: AtencionEntity): Long

    /**
     * Actualiza una atención existente.
     */
    suspend fun actualizar(atencion: AtencionEntity)

    /**
     * Obtiene una atención por ID.
     */
    suspend fun obtenerPorId(id: Long): AtencionEntity?

    /**
     * Observa todas las atenciones.
     */
    fun observarTodas(): Flow<List<AtencionListado>>

    /**
     * Observa las atenciones de un tipo determinado.
     */
    fun observarPorTipo(tipo: String): Flow<List<AtencionListado>>
}