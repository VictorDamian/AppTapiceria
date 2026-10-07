package com.tapiceria.app.data.repository

import com.tapiceria.app.data.local.dao.TrabajoDao
import com.tapiceria.app.data.local.entity.TrabajoEntity
import com.tapiceria.app.domain.model.CotizacionTrabajoOpcion
import com.tapiceria.app.domain.model.TrabajoListado
import com.tapiceria.app.domain.repository.TrabajoRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación del repositorio de trabajos mediante Room.
 */
class TrabajoRepositoryImpl(
    private val trabajoDao: TrabajoDao
) : TrabajoRepository {

    override suspend fun insertar(
        trabajo: TrabajoEntity
    ): Long {
        return trabajoDao.insertar(trabajo)
    }

    override suspend fun actualizar(
        trabajo: TrabajoEntity
    ) {
        trabajoDao.actualizar(trabajo)
    }

    override suspend fun obtenerPorId(
        id: Long
    ): TrabajoEntity? {
        return trabajoDao.obtenerPorId(id)
    }

    override suspend fun obtenerActivoPorCotizacion(
        cotizacionId: Long
    ): TrabajoEntity? {
        return trabajoDao.obtenerActivoPorCotizacion(
            cotizacionId
        )
    }

    override fun observarTodos():
            Flow<List<TrabajoListado>> {
        return trabajoDao.observarTodos()
    }

    override fun observarCotizacionesAceptadas():
            Flow<List<CotizacionTrabajoOpcion>> {
        return trabajoDao.observarCotizacionesAceptadas()
    }

    override suspend fun actualizarEstado(
        id: Long,
        estado: String
    ): Int {
        return trabajoDao.actualizarEstado(
            id,
            estado
        )
    }
}