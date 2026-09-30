
package com.tapiceria.app.data.repository

import com.tapiceria.app.data.local.dao.AtencionDao
import com.tapiceria.app.data.local.entity.AtencionEntity
import com.tapiceria.app.domain.model.AtencionListado
import com.tapiceria.app.domain.repository.AtencionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación del repositorio de atenciones mediante Room.
 */
class AtencionRepositoryImpl(
    private val dao: AtencionDao
) : AtencionRepository {

    override suspend fun insertar(atencion: AtencionEntity): Long =
        dao.insertar(atencion)

    override suspend fun obtenerPorId(id: Long): AtencionEntity? =
        dao.obtenerPorId(id)

    override fun observarTodas(): Flow<List<AtencionListado>> =
        dao.observarTodas()

    override fun observarPorTipo(
        tipo: String
    ): Flow<List<AtencionListado>> =
        dao.observarPorTipo(tipo)
}