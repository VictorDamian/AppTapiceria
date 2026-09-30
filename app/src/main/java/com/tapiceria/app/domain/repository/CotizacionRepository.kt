
package com.tapiceria.app.domain.repository

import com.tapiceria.app.data.local.entity.CotizacionEntity
import com.tapiceria.app.domain.model.CotizacionListado
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para consultar y administrar cotizaciones.
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

    fun observarTodas(): Flow<List<CotizacionListado>>

    suspend fun marcarVencidas(ahora: Long): Int
}