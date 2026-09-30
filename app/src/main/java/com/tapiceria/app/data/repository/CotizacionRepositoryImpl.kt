
package com.tapiceria.app.data.repository

import com.tapiceria.app.data.local.dao.CotizacionDao
import com.tapiceria.app.data.local.entity.CotizacionEntity
import com.tapiceria.app.domain.repository.CotizacionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación del repositorio de cotizaciones mediante Room.
 */
class CotizacionRepositoryImpl(
    private val dao: CotizacionDao
) : CotizacionRepository {

    override suspend fun insertar(cotizacion: CotizacionEntity): Long =
        dao.insertar(cotizacion)

    override suspend fun actualizar(cotizacion: CotizacionEntity) {
        dao.actualizar(cotizacion)
    }

    override suspend fun obtenerPorId(id: Long): CotizacionEntity? =
        dao.obtenerPorId(id)

    override fun observarPorAtencion(
        atencionId: Long
    ): Flow<List<CotizacionEntity>> =
        dao.observarPorAtencion(atencionId)

    override fun observarPorEstado(
        estado: String
    ): Flow<List<CotizacionEntity>> =
        dao.observarPorEstado(estado)

    override fun observarTodas(): Flow<List<CotizacionEntity>> =
        dao.observarTodas()
}