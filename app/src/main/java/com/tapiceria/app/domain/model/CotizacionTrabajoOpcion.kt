
package com.tapiceria.app.domain.model

/**
 * Datos mínimos de una cotización aceptada que puede convertirse
 * en un trabajo.
 */
data class CotizacionTrabajoOpcion(
    val id: Long,
    val clienteId: Long,
    val nombreCliente: String,
    val folio: String,
    val descripcion: String,
    val importeCentavos: Long
)