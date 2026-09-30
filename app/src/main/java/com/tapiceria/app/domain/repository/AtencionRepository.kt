
package com.tapiceria.app.domain.repository

import com.tapiceria.app.data.local.entity.AtencionEntity
import com.tapiceria.app.domain.model.AtencionListado
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para gestionar consultas y solicitudes de cotización.
 */
interface AtencionRepository {

    suspend fun insertar(atencion: AtencionEntity): Long

    suspend fun obtenerPorId(id: Long): AtencionEntity?

    fun observarTodas(): Flow<List<AtencionListado>>

    fun observarPorTipo(tipo: String): Flow<List<AtencionListado>>
}