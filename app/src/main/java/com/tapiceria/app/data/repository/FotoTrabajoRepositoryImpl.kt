
package com.tapiceria.app.data.repository

import com.tapiceria.app.data.local.dao.FotoTrabajoDao
import com.tapiceria.app.data.local.entity.FotoTrabajoEntity
import com.tapiceria.app.domain.repository.FotoTrabajoRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación del repositorio mediante Room.
 */
class FotoTrabajoRepositoryImpl(
    private val fotoTrabajoDao: FotoTrabajoDao
) : FotoTrabajoRepository {

    override suspend fun insertar(foto: FotoTrabajoEntity): Long {
        return fotoTrabajoDao.insertar(foto)
    }

    override fun observarPorTrabajo(
        trabajoId: Long
    ): Flow<List<FotoTrabajoEntity>> {
        return fotoTrabajoDao.observarPorTrabajo(trabajoId)
    }

    override suspend fun obtenerPorId(id: Long): FotoTrabajoEntity? {
        return fotoTrabajoDao.obtenerPorId(id)
    }

    override suspend fun eliminarPorId(id: Long): Int {
        return fotoTrabajoDao.eliminarPorId(id)
    }
}