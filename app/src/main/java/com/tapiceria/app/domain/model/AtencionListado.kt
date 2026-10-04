package com.tapiceria.app.domain.model

/**
 * Datos necesarios para mostrar una atención en la interfaz.
 * No representa una tabla de la base de datos.
 */
data class AtencionListado(
    val id: Long,
    val clienteId: Long?,
    val nombreCliente: String,
    val tipo: String,
    val descripcion: String,
    val fechaAtencion: Long,
    val notas: String
)
