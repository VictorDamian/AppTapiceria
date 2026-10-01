
package com.tapiceria.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tapiceria.app.data.local.entity.PagoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PagoDao {

    /**
     * Registra un pago y devuelve el ID generado.
     */
    @Insert
    suspend fun insertar(pago: PagoEntity): Long

    /**
     * Obtiene el historial de pagos de un trabajo.
     */
    @Query("""
        SELECT *
        FROM pagos
        WHERE trabajoId = :trabajoId
        ORDER BY fechaPago DESC
    """)
    fun observarPorTrabajo(
        trabajoId: Long
    ): Flow<List<PagoEntity>>

    /**
     * Calcula el total pagado de un trabajo.
     * COALESCE devuelve cero cuando todavía no existen pagos.
     */
    @Query("""
        SELECT COALESCE(SUM(importeCentavos), 0)
        FROM pagos
        WHERE trabajoId = :trabajoId
    """)
    fun observarTotalPagado(
        trabajoId: Long
    ): Flow<Long>

    /**
     * Obtiene el total pagado en una consulta puntual.
     */
    @Query("""
        SELECT COALESCE(SUM(importeCentavos), 0)
        FROM pagos
        WHERE trabajoId = :trabajoId
    """)
    suspend fun obtenerTotalPagado(
        trabajoId: Long
    ): Long

    /**
     * Elimina un pago por su identificador.
     */
    @Query("DELETE FROM pagos WHERE id = :id")
    suspend fun eliminarPorId(id: Long): Int
}