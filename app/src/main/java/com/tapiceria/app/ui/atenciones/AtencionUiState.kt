
package com.tapiceria.app.ui.atenciones

import com.tapiceria.app.data.local.entity.ClienteEntity
import com.tapiceria.app.domain.model.AtencionListado

/**
 * Estado de la pantalla de atenciones.
 *
 * Mantiene separados los datos del formulario, los catálogos
 * y los mensajes que se muestran al usuario.
 */
data class AtencionUiState(
    val atenciones: List<AtencionListado> = emptyList(),
    val clientes: List<ClienteEntity> = emptyList(),
    val clienteSeleccionadoId: Long? = null,
    val tipoSeleccionado: String = "CONSULTA",
    val descripcion: String = "",
    val notas: String = "",
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)