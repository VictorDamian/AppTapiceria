
package com.tapiceria.app.domain.model

/**
 * Información combinada para mostrar una cotización en pantalla.
 *
 * No es una entidad de Room: representa los datos que necesita la UI.
 */
data class CotizacionListado(
    val id: Long,
    val atencionId: Long,
    val folio: String,
    val nombreCliente: String,
    val descripcionAtencion: String,
    val descripcionCotizacion: String,
    val importeCentavos: Long,
    val fechaCreacion: Long,
    val fechaVigencia: Long?,
    val estado: String
)