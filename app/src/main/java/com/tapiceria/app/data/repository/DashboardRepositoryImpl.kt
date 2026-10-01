
package com.tapiceria.app.data.repository

import com.tapiceria.app.data.local.dao.DashboardDao
import com.tapiceria.app.domain.model.CotizacionPendienteDashboard
import com.tapiceria.app.domain.model.DashboardResumen
import com.tapiceria.app.domain.model.EntregaProximaDashboard
import com.tapiceria.app.domain.model.SaldoPendienteDashboard
import com.tapiceria.app.domain.model.TrabajoDashboard
import com.tapiceria.app.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación del repositorio usando consultas Room.
 */
class DashboardRepositoryImpl(
    private val dashboardDao: DashboardDao
) : DashboardRepository {

    override fun observarResumen(
        ahora: Long,
        limiteEntrega: Long
    ): Flow<DashboardResumen> {
        return dashboardDao.observarResumen(ahora, limiteEntrega)
    }

    override fun observarTrabajosRecientes(): Flow<List<TrabajoDashboard>> {
        return dashboardDao.observarTrabajosRecientes()
    }

    override fun observarCotizacionesPendientes(
        ahora: Long
    ): Flow<List<CotizacionPendienteDashboard>> {
        return dashboardDao.observarCotizacionesPendientes(ahora)
    }

    override fun observarEntregasProximas(
        ahora: Long,
        limiteEntrega: Long
    ): Flow<List<EntregaProximaDashboard>> {
        return dashboardDao.observarEntregasProximas(ahora, limiteEntrega)
    }

    override fun observarSaldosPendientes(): Flow<List<SaldoPendienteDashboard>> {
        return dashboardDao.observarSaldosPendientes()
    }
}