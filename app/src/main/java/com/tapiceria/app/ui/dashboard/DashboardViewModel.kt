
package com.tapiceria.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tapiceria.app.domain.repository.DashboardRepository
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Coordina las consultas necesarias para el Dashboard.
 */
class DashboardViewModel(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        cargarDashboard()
    }

    /**
     * Observa los indicadores y las listas de la pantalla.
     */
    private fun cargarDashboard() {
        viewModelScope.launch {
            try {
                val ahora = System.currentTimeMillis()

                // El límite corresponde a siete días a partir del momento actual.
                val limiteEntrega = ahora + TimeUnit.DAYS.toMillis(7)

                // Cada Flow se mantiene suscrito para reflejar cambios de Room.
                launch {
                    dashboardRepository
                        .observarResumen(ahora, limiteEntrega)
                        .catch { manejarError(it) }
                        .collect { resumen ->
                            _uiState.update {
                                it.copy(resumen = resumen, cargando = false)
                            }
                        }
                }

                launch {
                    dashboardRepository
                        .observarTrabajosRecientes()
                        .catch { manejarError(it) }
                        .collect { trabajos ->
                            _uiState.update {
                                it.copy(
                                    trabajosRecientes = trabajos,
                                    cargando = false
                                )
                            }
                        }
                }

                launch {
                    dashboardRepository
                        .observarCotizacionesPendientes(ahora)
                        .catch { manejarError(it) }
                        .collect { cotizaciones ->
                            _uiState.update {
                                it.copy(
                                    cotizacionesPendientes = cotizaciones,
                                    cargando = false
                                )
                            }
                        }
                }

                launch {
                    dashboardRepository
                        .observarEntregasProximas(ahora, limiteEntrega)
                        .catch { manejarError(it) }
                        .collect { entregas ->
                            _uiState.update {
                                it.copy(
                                    entregasProximas = entregas,
                                    cargando = false
                                )
                            }
                        }
                }

                launch {
                    dashboardRepository
                        .observarSaldosPendientes()
                        .catch { manejarError(it) }
                        .collect { saldos ->
                            _uiState.update {
                                it.copy(
                                    saldosPendientes = saldos,
                                    cargando = false
                                )
                            }
                        }
                }
            } catch (ex: Exception) {
                manejarError(ex)
            }
        }
    }

    private fun manejarError(ex: Throwable) {
        _uiState.update {
            it.copy(
                cargando = false,
                error = ex.message ?: "No fue posible cargar el Dashboard."
            )
        }
    }
}