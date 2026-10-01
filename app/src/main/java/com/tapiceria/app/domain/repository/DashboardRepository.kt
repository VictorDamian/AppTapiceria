
package com.tapiceria.app.domain.repository

import com.tapiceria.app.domain.model.CotizacionPendienteDashboard
import com.tapiceria.app.domain.model.DashboardResumen
import com.tapiceria.app.domain.model.EntregaProximaDashboard
import com.tapiceria.app.domain.model.SaldoPendienteDashboard
import com.tapiceria.app.domain.model.TrabajoDashboard
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de acceso a los datos del Dashboard.
 */
interface DashboardRepository {

    fun observarResumen(
        ahora: Long,
        limiteEntrega: Long
    ): Flow<DashboardResumen>

    fun observarTrabajosRecientes(): Flow<List<TrabajoDashboard>>

    fun observarCotizacionesPendientes(
        ahora: Long
    ): Flow<List<CotizacionPendienteDashboard>>

    fun observarEntregasProximas(
        ahora: Long,
        limiteEntrega: Long
    ): Flow<List<EntregaProximaDashboard>>

    fun observarSaldosPendientes(): Flow<List<SaldoPendienteDashboard>>
}