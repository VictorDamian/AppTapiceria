
package com.tapiceria.app.domain.model

/**
 * Datos mínimos de un trabajo para mostrarlo en el Dashboard.
 */
data class TrabajoDashboard(
    val id: Long,
    val folio: String,
    val nombreCliente: String,
    val descripcion: String,
    val importeCentavos: Long,
    val estado: String,
    val fechaRecepcion: Long,
    val fechaEntregaEstimada: Long?
)