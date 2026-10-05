
package com.tapiceria.app.ui.cotizaciones

import com.tapiceria.app.domain.model.AtencionListado
import com.tapiceria.app.domain.model.CotizacionListado

/**
 * Estado de la pantalla de cotizaciones.
 */
data class CotizacionUiState(
    val cotizaciones: List<CotizacionListado> = emptyList(),
    val atenciones: List<AtencionListado> = emptyList(),

    // Cotización que se está editando.
    val cotizacionEditandoId: Long? = null,

    val atencionSeleccionadaId: Long? = null,

    val textoBusquedaAtencion: String = "",

    val descripcion: String = "",
    val importe: String = "",

    // Formato interno: yyyy-MM-dd.
    val fechaVigencia: String = "",

    val cargando: Boolean = true,
    val guardando: Boolean = false,

    val error: String? = null,
    val mensaje: String? = null
)