
package com.tapiceria.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tapiceria.app.data.local.entity.CotizacionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Operaciones de persistencia para cotizaciones.
 */
@Dao
interface CotizacionDao {

    /** Registra una cotización y devuelve el ID generado. */
    @Insert
    suspend fun insertar(cotizacion: CotizacionEntity): Long

    /** Actualiza el estado o los datos de una cotización existente. */
    @Update
    suspend fun actualizar(cotizacion: CotizacionEntity)

    /** Busca una cotización por ID. */
    @Query("SELECT * FROM cotizaciones WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): CotizacionEntity?

    /** Observa las cotizaciones de una atención. */
    @Query("""
        SELECT * FROM cotizaciones
        WHERE atencionId = :atencionId
        ORDER BY fechaCreacion DESC
    """)
    fun observarPorAtencion(atencionId: Long): Flow<List<CotizacionEntity>>

    /** Muestra las cotizaciones con un estado determinado. */
    @Query("""
        SELECT * FROM cotizaciones
        WHERE estado = :estado
        ORDER BY fechaCreacion DESC
    """)
    fun observarPorEstado(estado: String): Flow<List<CotizacionEntity>>

    /** Recupera todas las cotizaciones, de la más reciente a la más antigua. */
    @Query("""
        SELECT * FROM cotizaciones
        ORDER BY fechaCreacion DESC
    """)
    fun observarTodas(): Flow<List<CotizacionEntity>>
}