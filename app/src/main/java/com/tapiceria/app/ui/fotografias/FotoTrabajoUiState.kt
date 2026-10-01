
package com.tapiceria.app.ui.fotografias

import com.tapiceria.app.data.local.entity.FotoTrabajoEntity
import com.tapiceria.app.domain.model.TrabajoListado

/**
 * Estado de la pantalla de fotografías.
 */
data class FotoTrabajoUiState(
    val trabajos: List<TrabajoListado> = emptyList(),
    val trabajoSeleccionadoId: Long? = null,
    val fotografias: List<FotoTrabajoEntity> = emptyList(),
    val tipoSeleccionado: String = "ANTES",
    val descripcion: String = "",
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)