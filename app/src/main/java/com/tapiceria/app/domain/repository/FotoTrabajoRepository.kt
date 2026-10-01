
package com.tapiceria.app.domain.repository

import com.tapiceria.app.data.local.entity.FotoTrabajoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Define las operaciones disponibles para las fotografías de trabajos.
 */
interface FotoTrabajoRepository {

    suspend fun insertar(foto: FotoTrabajoEntity): Long

    fun observarPorTrabajo(
        trabajoId: Long
    ): Flow<List<FotoTrabajoEntity>>

    suspend fun obtenerPorId(id: Long): FotoTrabajoEntity?

    suspend fun eliminarPorId(id: Long): Int
}