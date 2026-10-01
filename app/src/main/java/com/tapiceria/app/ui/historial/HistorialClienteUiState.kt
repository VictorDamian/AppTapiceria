package com.tapiceria.app.ui.historial

import com.tapiceria.app.domain.model.HistorialCliente

/**
 * Estado de la pantalla del historial.
 */
data class HistorialClienteUiState(
    val cargando: Boolean = true,
    val historial: HistorialCliente = HistorialCliente(),
    val error: String? = null
)