
package com.tapiceria.app.domain.repository

import com.tapiceria.app.data.local.entity.TrabajoEntity
import com.tapiceria.app.domain.model.CotizacionTrabajoOpcion
import com.tapiceria.app.domain.model.TrabajoListado
import kotlinx.coroutines.flow.Flow

/**
 * Define las operaciones disponibles para administrar trabajos.
 */
interface TrabajoRepository {

    suspend fun insertar(trabajo: TrabajoEntity): Long

    suspend fun actualizar(trabajo: TrabajoEntity)

    suspend fun obtenerPorId(id: Long): TrabajoEntity?

    fun observarTodos(): Flow<List<TrabajoListado>>

    fun observarCotizacionesAceptadas():
            Flow<List<CotizacionTrabajoOpcion>>

    suspend fun actualizarEstado(id: Long, estado: String): Int
}