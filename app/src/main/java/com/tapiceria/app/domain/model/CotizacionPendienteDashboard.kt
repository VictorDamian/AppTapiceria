
package com.tapiceria.app.domain.model

/**
 * Cotización que todavía espera una respuesta del cliente.
 */
data class CotizacionPendienteDashboard(
    val id: Long,
    val folio: String,
    val nombreCliente: String,
    val descripcion: String,
    val importeCentavos: Long,
    val fechaCreacion: Long,
    val fechaVigencia: Long?
)