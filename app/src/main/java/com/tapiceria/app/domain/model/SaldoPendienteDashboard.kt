
package com.tapiceria.app.domain.model

/**
 * Resume cuánto falta por pagar de un trabajo.
 */
data class SaldoPendienteDashboard(
    val id: Long,
    val folio: String,
    val nombreCliente: String,
    val importeCentavos: Long,
    val totalPagadoCentavos: Long,
    val saldoPendienteCentavos: Long
)