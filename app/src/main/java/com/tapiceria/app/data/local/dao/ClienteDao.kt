package com.tapiceria.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tapiceria.app.data.local.entity.ClienteEntity

/**
 * Define las operaciones de acceso a clientes.
 * Room genera automáticamente su implementación.
 */
@Dao
interface ClienteDao {

    /**
     * Registra un cliente y devuelve el identificador generado.
     */
    @Insert
    suspend fun insertar(cliente: ClienteEntity): Long

    /**
     * Actualiza los datos de un cliente existente.
     */
    @Update
    suspend fun actualizar(cliente: ClienteEntity)

    /**
     * Busca un cliente por su identificador.
     */
    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): ClienteEntity?

    /**
     * Obtiene los clientes activos ordenados alfabéticamente.
     */
    @Query("""
        SELECT * FROM clientes
        WHERE activo = 1
        ORDER BY nombre COLLATE NOCASE ASC
    """)
    suspend fun obtenerActivos(): List<ClienteEntity>

    /**
     * Busca por nombre o teléfono.
     * El patrón permite encontrar coincidencias parciales.
     */
    @Query("""
        SELECT * FROM clientes
        WHERE activo = 1
          AND (
              nombre LIKE '%' || :texto || '%'
              OR telefono LIKE '%' || :texto || '%'
          )
        ORDER BY nombre COLLATE NOCASE ASC
    """)
    suspend fun buscar(texto: String): List<ClienteEntity>

    /**
     * Desactiva el cliente sin borrar su historial.
     */
    @Query("UPDATE clientes SET activo = 0 WHERE id = :id")
    suspend fun desactivar(id: Long)
}