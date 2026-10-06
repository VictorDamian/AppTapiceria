package com.tapiceria.app.ui.cotizaciones

import com.tapiceria.app.domain.model.AtencionListado
import com.tapiceria.app.domain.model.CotizacionListado

/**
 * Estado de la pantalla de cotizaciones.
 */
data class CotizacionUiState(
    val cotizaciones: List<CotizacionListado> = emptyList(),

    /**
     * Atenciones de tipo COTIZACION.
     */
    val atenciones: List<AtencionListado> = emptyList(),

    /**
     * Texto utilizado para buscar una solicitud.
     */
    val textoBusquedaAtencion: String = "",

    /**
     * Atención seleccionada para la cotización.
     */
    val atencionSeleccionadaId: Long? = null,

    /**
     * ID de la cotización que estamos editando.
     *
     * Null significa que estamos creando una nueva.
     */
    val cotizacionEditandoId: Long? = null,

    val descripcion: String = "",

    val importe: String = "",

    /**
     * Fecha en formato yyyy-MM-dd.
     */
    val fechaVigencia: String = "",

    val cargando: Boolean = true,

    val guardando: Boolean = false,

    val error: String? = null,

    val mensaje: String? = null
)