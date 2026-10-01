
package com.tapiceria.app.domain.repository

import com.tapiceria.app.data.local.entity.PagoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Define las operaciones disponibles para administrar pagos.
 * La interfaz permite cambiar la fuente de datos en el futuro.
 */
interface PagoRepository {

    suspend fun insertar(pago: PagoEntity): Long

    fun observarPorTrabajo(
        trabajoId: Long
    ): Flow<List<PagoEntity>>

    fun observarTotalPagado(
        trabajoId: Long
    ): Flow<Long>

    suspend fun obtenerTotalPagado(
        trabajoId: Long
    ): Long

    suspend fun eliminarPorId(id: Long): Int
}