package com.tapiceria.app.ui.trabajos

import com.tapiceria.app.data.local.entity.ClienteEntity
import com.tapiceria.app.domain.model.CotizacionTrabajoOpcion
import com.tapiceria.app.domain.model.TrabajoListado

/**
 * Estado del formulario y listado de trabajos.
 */
data class TrabajoUiState(
    val trabajos: List<TrabajoListado> = emptyList(),

    val clientes: List<ClienteEntity> = emptyList(),

    val cotizaciones: List<CotizacionTrabajoOpcion> = emptyList(),

    val trabajoEditandoId: Long? = null,

    val clienteSeleccionadoId: Long? = null,

    val cotizacionSeleccionadaId: Long? = null,

    val textoBusquedaCliente: String = "",

    val textoBusquedaCotizacion: String = "",

    val descripcion: String = "",

    val importe: String = "",

    val fechaEntregaEstimada: String = "",

    val notas: String = "",

    val estadoSeleccionado: String = "PENDIENTE",

    val cargando: Boolean = true,

    val guardando: Boolean = false,

    val error: String? = null,

    val mensaje: String? = null
)