
package com.tapiceria.app.data.repository

import com.tapiceria.app.data.local.dao.PagoDao
import com.tapiceria.app.data.local.entity.PagoEntity
import com.tapiceria.app.domain.repository.PagoRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación local del repositorio de pagos mediante Room.
 */
class PagoRepositoryImpl(
    private val pagoDao: PagoDao
) : PagoRepository {

    override suspend fun insertar(pago: PagoEntity): Long {
        return pagoDao.insertar(pago)
    }

    override fun observarPorTrabajo(
        trabajoId: Long
    ): Flow<List<PagoEntity>> {
        return pagoDao.observarPorTrabajo(trabajoId)
    }

    override fun observarTotalPagado(
        trabajoId: Long
    ): Flow<Long> {
        return pagoDao.observarTotalPagado(trabajoId)
    }

    override suspend fun obtenerTotalPagado(
        trabajoId: Long
    ): Long {
        return pagoDao.obtenerTotalPagado(trabajoId)
    }

    override suspend fun eliminarPorId(id: Long): Int {
        return pagoDao.eliminarPorId(id)
    }
}