
package com.tapiceria.app.ui.pagos

import com.tapiceria.app.data.local.entity.PagoEntity
import com.tapiceria.app.domain.model.TrabajoListado

/**
 * Estado que consume la pantalla de pagos.
 */
data class PagoUiState(
    val trabajos: List<TrabajoListado> = emptyList(),
    val trabajoSeleccionadoId: Long? = null,
    val pagos: List<PagoEntity> = emptyList(),
    val importeTrabajoCentavos: Long = 0L,
    val totalPagadoCentavos: Long = 0L,
    val importe: String = "",
    val metodo: String = "EFECTIVO",
    val referencia: String = "",
    val notas: String = "",
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
) {
    /**
     * Calcula el saldo sin permitir resultados negativos.
     */
    val saldoPendienteCentavos: Long
        get() = (importeTrabajoCentavos - totalPagadoCentavos)
            .coerceAtLeast(0L)
}