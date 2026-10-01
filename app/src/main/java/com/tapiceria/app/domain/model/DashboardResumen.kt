
package com.tapiceria.app.domain.model

/**
 * Indicadores generales que aparecen en la parte superior.
 */
data class DashboardResumen(
    val trabajosActivos: Int = 0,
    val cotizacionesPendientes: Int = 0,
    val entregasProximas: Int = 0,
    val trabajosConSaldo: Int = 0
)