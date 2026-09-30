
package com.tapiceria.app.data.repository

import com.tapiceria.app.data.local.dao.ClienteDao
import com.tapiceria.app.data.local.entity.ClienteEntity
import com.tapiceria.app.domain.repository.ClienteRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementa el contrato del repositorio usando Room.
 *
 * La interfaz de dominio no necesita conocer los detalles
 * de cómo se almacenan los clientes.
 */
class ClienteRepositoryImpl(
    private val clienteDao: ClienteDao
) : ClienteRepository {

    override suspend fun insertar(cliente: ClienteEntity): Long {
        // Room ejecuta la operación de escritura fuera del hilo principal.
        return clienteDao.insertar(cliente)
    }

    override suspend fun actualizar(cliente: ClienteEntity) {
        clienteDao.actualizar(cliente)
    }

    override suspend fun obtenerPorId(id: Long): ClienteEntity? {
        return clienteDao.obtenerPorId(id)
    }

    override fun observarActivos(): Flow<List<ClienteEntity>> {
        return clienteDao.observarActivos()
    }

    override fun buscar(texto: String): Flow<List<ClienteEntity>> {
        return clienteDao.buscar(texto.trim())
    }

    override suspend fun desactivar(id: Long) {
        clienteDao.desactivar(id)
    }
}