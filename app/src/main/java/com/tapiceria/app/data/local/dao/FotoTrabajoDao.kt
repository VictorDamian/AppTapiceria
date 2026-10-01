
package com.tapiceria.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tapiceria.app.data.local.entity.FotoTrabajoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Operaciones de acceso a las fotografías asociadas a un trabajo.
 */
@Dao
interface FotoTrabajoDao {

    @Insert
    suspend fun insertar(foto: FotoTrabajoEntity): Long

    /**
     * Observa las fotografías de un trabajo, de la más reciente
     * a la más antigua.
     */
    @Query("""
        SELECT *
        FROM fotos_trabajo
        WHERE trabajoId = :trabajoId
        ORDER BY fechaRegistro DESC
    """)
    fun observarPorTrabajo(
        trabajoId: Long
    ): Flow<List<FotoTrabajoEntity>>

    @Query("""
        SELECT *
        FROM fotos_trabajo
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun obtenerPorId(id: Long): FotoTrabajoEntity?

    @Query("""
        DELETE FROM fotos_trabajo
        WHERE id = :id
    """)
    suspend fun eliminarPorId(id: Long): Int
}