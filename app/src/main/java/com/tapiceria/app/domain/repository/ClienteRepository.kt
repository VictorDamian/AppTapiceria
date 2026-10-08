package com.tapiceria.app.domain.repository

import com.tapiceria.app.data.local.entity.ClienteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Define las operaciones disponibles para gestionar clientes.
 *
 * La interfaz permite cambiar Room por una API en el futuro
 * sin modificar las pantallas que consumen este contrato.
 */
interface ClienteRepository {

    /** Registra un cliente y devuelve su identificador. */
    suspend fun insertar(cliente: ClienteEntity): Long

    /** Actualiza los datos de un cliente existente. */
    suspend fun actualizar(cliente: ClienteEntity)

    /** Consulta un cliente por su identificador. */
    suspend fun obtenerPorId(id: Long): ClienteEntity?

    /**
     * Obtiene clientes activos e inactivos.
     */
    fun observarTodos(): Flow<List<ClienteEntity>>

    /**
     * Obtiene únicamente clientes activos.
     */
    fun observarActivos(): Flow<List<ClienteEntity>>

    /** Busca clientes activos por nombre o teléfono. */
    fun buscar(texto: String): Flow<List<ClienteEntity>>

    /** Desactiva un cliente sin eliminar su historial. */
    suspend fun desactivar(id: Long)

    suspend fun activar(id: Long)

    /**
     * Indica si el cliente tiene cotizaciones que impiden
     * realizar una baja lógica.
     */
    suspend fun tieneCotizacionesBloqueantes(
        clienteId: Long
    ): Boolean
}