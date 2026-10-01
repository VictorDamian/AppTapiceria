package com.tapiceria.app.domain.model

/**
 * Información combinada que necesita la pantalla de trabajos.
 */
data class TrabajoListado(
    val id: Long,
    val clienteId: Long,
    val cotizacionId: Long?,
    val folio: String,
    val nombreCliente: String,
    val descripcion: String,
    val importeCentavos: Long,
    val fechaRecepcion: Long,
    val fechaEntregaEstimada: Long?,
    val fechaEntregaReal: Long?,
    val estado: String,
    val notas: String
)