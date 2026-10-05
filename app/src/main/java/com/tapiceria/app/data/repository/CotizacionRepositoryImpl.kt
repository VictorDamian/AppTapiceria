
package com.tapiceria.app.data.repository

import com.tapiceria.app.data.local.dao.CotizacionDao
import com.tapiceria.app.data.local.entity.CotizacionEntity
import com.tapiceria.app.domain.model.CotizacionListado
import com.tapiceria.app.domain.repository.CotizacionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación del repositorio utilizando Room.
 */
class CotizacionRepositoryImpl(
    private val cotizacionDao: CotizacionDao
) : CotizacionRepository {

    override suspend fun insertar(
        cotizacion: CotizacionEntity
    ): Long {
        return cotizacionDao.insertar(cotizacion)
    }

    override suspend fun actualizar(
        cotizacion: CotizacionEntity
    ) {
        cotizacionDao.actualizar(cotizacion)
    }

    override suspend fun obtenerPorId(
        id: Long
    ): CotizacionEntity? {
        return cotizacionDao.obtenerPorId(id)
    }

    override fun observarPorAtencion(
        atencionId: Long
    ): Flow<List<CotizacionEntity>> {
        return cotizacionDao.observarPorAtencion(atencionId)
    }

    override fun observarPorEstado(
        estado: String
    ): Flow<List<CotizacionEntity>> {
        return cotizacionDao.observarPorEstado(estado)
    }

    override fun observarTodas(): Flow<List<CotizacionListado>> {
        return cotizacionDao.observarTodas()
    }

    override suspend fun marcarVencidas(ahora: Long): Int {
        return cotizacionDao.marcarVencidas(ahora)
    }

    override suspend fun obtenerPorAtencion(
        atencionId: Long
    ): CotizacionEntity? {
        return cotizacionDao.obtenerPorAtencion(atencionId)
    }
}