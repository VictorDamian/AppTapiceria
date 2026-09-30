
package com.tapiceria.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tapiceria.app.data.local.entity.ClienteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Define las consultas y operaciones de persistencia de clientes.
 * Room genera la implementación de esta interfaz.
 */
@Dao
interface ClienteDao {

    /** Inserta un cliente y devuelve el ID generado por SQLite. */
    @Insert
    suspend fun insertar(cliente: ClienteEntity): Long

    /** Actualiza los campos del cliente cuyo ID ya existe. */
    @Update
    suspend fun actualizar(cliente: ClienteEntity)

    /** Obtiene un cliente por ID o null si no existe. */
    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): ClienteEntity?

    /**
     * Emite nuevamente la lista cuando Room detecta cambios
     * en la tabla clientes.
     */
    @Query("""
        SELECT * FROM clientes
        WHERE activo = 1
        ORDER BY nombre COLLATE NOCASE ASC
    """)
    fun observarActivos(): Flow<List<ClienteEntity>>

    /** Busca por nombre o teléfono, ignorando clientes inactivos. */
    @Query("""
        SELECT * FROM clientes
        WHERE activo = 1
          AND (
              nombre LIKE '%' || :texto || '%'
              OR telefono LIKE '%' || :texto || '%'
          )
        ORDER BY nombre COLLATE NOCASE ASC
    """)
    fun buscar(texto: String): Flow<List<ClienteEntity>>

    /** Conserva el historial y oculta al cliente de las listas activas. */
    @Query("UPDATE clientes SET activo = 0 WHERE id = :id")
    suspend fun desactivar(id: Long)
}