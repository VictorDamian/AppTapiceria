
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
     * Obtiene todos los clientes, activos e inactivos.
     *
     * Esto permite administrar nuevamente un cliente desactivado.
    */
    @Query("""
    SELECT *
    FROM clientes
    ORDER BY activo DESC, nombre COLLATE NOCASE ASC
    """)
    fun observarTodos(): Flow<List<ClienteEntity>>

    /**
     * Mantiene disponible la consulta original para otras pantallas
     * que solamente necesiten clientes activos.
     */
    @Query("""
        SELECT * FROM clientes
        WHERE activo = 1
        ORDER BY nombre COLLATE NOCASE ASC
    """)
    fun observarActivos(): Flow<List<ClienteEntity>>

    /**
     * Busca clientes activos e inactivos.
     */
    @Query("""
        SELECT *
        FROM clientes
        WHERE nombre LIKE '%' || :texto || '%'
           OR telefono LIKE '%' || :texto || '%'
        ORDER BY activo DESC, nombre COLLATE NOCASE ASC
    """)
    fun buscar(texto: String): Flow<List<ClienteEntity>>

    /** Conserva el historial y oculta al cliente de las listas activas. */
    @Query("UPDATE clientes SET activo = 0 WHERE id = :id")
    suspend fun desactivar(id: Long)

    @Query("UPDATE clientes SET activo = 1 WHERE id = :id")
    suspend fun activar(id: Long)

    /**
     * Determina si el cliente tiene alguna cotización que impide
     * realizar una baja lógica.
     *
     * RECHAZADA y CANCELADA no bloquean la baja.
     *
     * Cualquier otro estado, incluyendo PENDIENTE, ACEPTADA
     * y VENCIDA, mantiene bloqueado al cliente.
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1
            FROM cotizaciones co
            INNER JOIN atenciones a
                ON a.id = co.atencionId
            WHERE a.clienteId = :clienteId
              AND co.estado NOT IN ('RECHAZADA', 'CANCELADA')
        )
    """)
    suspend fun tieneCotizacionesBloqueantes(
        clienteId: Long
    ): Boolean
}