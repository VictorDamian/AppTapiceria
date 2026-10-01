
package com.tapiceria.app.domain.model

/**
 * Trabajo con una fecha estimada de entrega próxima.
 */
data class EntregaProximaDashboard(
    val id: Long,
    val folio: String,
    val nombreCliente: String,
    val fechaEntregaEstimada: Long,
    val estado: String
)