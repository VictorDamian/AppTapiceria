
package com.tapiceria.app.ui.dashboard

import com.tapiceria.app.domain.model.CotizacionPendienteDashboard
import com.tapiceria.app.domain.model.DashboardResumen
import com.tapiceria.app.domain.model.EntregaProximaDashboard
import com.tapiceria.app.domain.model.SaldoPendienteDashboard
import com.tapiceria.app.domain.model.TrabajoDashboard

/**
 * Información que consume la pantalla principal.
 */
data class DashboardUiState(
    val resumen: DashboardResumen = DashboardResumen(),
    val trabajosRecientes: List<TrabajoDashboard> = emptyList(),
    val cotizacionesPendientes: List<CotizacionPendienteDashboard> = emptyList(),
    val entregasProximas: List<EntregaProximaDashboard> = emptyList(),
    val saldosPendientes: List<SaldoPendienteDashboard> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)