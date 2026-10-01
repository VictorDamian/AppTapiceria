
package com.tapiceria.app.domain.repository

import com.tapiceria.app.domain.model.HistorialCliente
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para consultar el historial de un cliente.
 */
interface HistorialClienteRepository {

    fun observarHistorial(clienteId: Long): Flow<HistorialCliente>
}