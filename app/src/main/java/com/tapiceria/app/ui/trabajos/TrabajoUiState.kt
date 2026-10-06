package com.tapiceria.app.ui.trabajos

import com.tapiceria.app.data.local.entity.ClienteEntity
import com.tapiceria.app.domain.model.CotizacionTrabajoOpcion
import com.tapiceria.app.domain.model.TrabajoListado

/**
 * Estado de la pantalla de trabajos.
 *
 * Centraliza los datos del formulario, búsquedas,
 * edición y listado.
 */
data class TrabajoUiState(
    val trabajos: List<TrabajoListado> = emptyList(),

    val clientes: List<ClienteEntity> = emptyList(),
    val cotizaciones: List<CotizacionTrabajoOpcion> = emptyList(),

    // Texto utilizado para buscar clientes.
    val textoBusquedaCliente: String = "",

    // Texto utilizado para buscar cotizaciones.
    val textoBusquedaCotizacion: String = "",

    val clienteSeleccionadoId: Long? = null,
    val cotizacionSeleccionadaId: Long? = null,

    // ID del trabajo que se está editando.
    // null significa que estamos registrando uno nuevo.
    val trabajoEditandoId: Long? = null,

    val descripcion: String = "",
    val importe: String = "",
    val fechaEntregaEstimada: String = "",
    val notas: String = "",

    val cargando: Boolean = true,
    val guardando: Boolean = false,

    val error: String? = null,
    val mensaje: String? = null
)