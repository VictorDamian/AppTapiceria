package com.tapiceria.app.domain.model

import com.tapiceria.app.data.local.entity.AtencionEntity
import com.tapiceria.app.data.local.entity.ClienteEntity
import com.tapiceria.app.data.local.entity.CotizacionEntity
import com.tapiceria.app.data.local.entity.FotoTrabajoEntity
import com.tapiceria.app.data.local.entity.PagoEntity
import com.tapiceria.app.data.local.entity.TrabajoEntity

/**
 * Información necesaria para mostrar el historial completo de un cliente.
 */
data class HistorialCliente(
    val cliente: ClienteEntity? = null,
    val atenciones: List<AtencionEntity> = emptyList(),
    val cotizaciones: List<CotizacionEntity> = emptyList(),
    val trabajos: List<TrabajoEntity> = emptyList(),
    val pagos: List<PagoEntity> = emptyList(),
    val fotografias: List<FotoTrabajoEntity> = emptyList()
) {
    /**
     * Total abonado por el cliente en todos sus trabajos.
     * Los importes se guardan en centavos para evitar errores de redondeo.
     */
    val totalPagadoCentavos: Long
        get() = pagos.sumOf { it.importeCentavos }

    /**
     * Saldo pendiente total de sus trabajos.
     * Se limita cada saldo a cero para no mostrar saldos negativos.
     */
    val saldoPendienteCentavos: Long
        get() {
            val totalTrabajos = trabajos.sumOf { it.importeCentavos }
            return (totalTrabajos - totalPagadoCentavos).coerceAtLeast(0L)
        }
}