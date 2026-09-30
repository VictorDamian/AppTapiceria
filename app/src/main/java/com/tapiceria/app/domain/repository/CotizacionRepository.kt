
package com.tapiceria.app.domain.repository

import com.tapiceria.app.data.local.entity.CotizacionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para las operaciones de cotizaciones.
 */
interface CotizacionRepository {

    suspend fun insertar(cotizacion: CotizacionEntity): Long

    suspend fun actualizar(cotizacion: CotizacionEntity)

    suspend fun obtenerPorId(id: Long): CotizacionEntity?

    fun observarPorAtencion(
        atencionId: Long
    ): Flow<List<CotizacionEntity>>

    fun observarPorEstado(
        estado: String
    ): Flow<List<CotizacionEntity>>

    fun observarTodas(): Flow<List<CotizacionEntity>>
}