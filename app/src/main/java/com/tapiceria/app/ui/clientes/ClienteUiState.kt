
package com.tapiceria.app.ui.clientes

import com.tapiceria.app.data.local.entity.ClienteEntity

/**
 * Estado que representa la información visible de la pantalla.
 * Centralizarlo facilita las pruebas y evita estados dispersos.
 *
 * Centralizar el estado evita tener información dispersa entre la pantalla
 * y el ViewModel.
 */
data class ClienteUiState(
    val clientes: List<ClienteEntity> = emptyList(),
    val textoBusqueda: String = "",
    val cargando: Boolean = true,
    val error: String? = null,
    val mensaje: String? = null,
    // Indica si existe una operación de guardado en curso.
    val guardando: Boolean = false,
)