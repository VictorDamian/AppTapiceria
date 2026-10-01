
package com.tapiceria.app.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tapiceria.app.domain.repository.HistorialClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * Administra la carga del historial y expone su estado a Compose.
 */
class HistorialClienteViewModel(
    private val clienteId: Long,
    private val repository: HistorialClienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialClienteUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarHistorial()
    }

    /**
     * Observa los datos del cliente y sus operaciones relacionadas.
     */
    private fun cargarHistorial() {
        viewModelScope.launch {
            repository.observarHistorial(clienteId)
                .onStart {
                    _uiState.value = _uiState.value.copy(
                        cargando = true,
                        error = null
                    )
                }
                .catch { ex ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = ex.message
                            ?: "No se pudo cargar el historial."
                    )
                }
                .collect { historial ->
                    _uiState.value = HistorialClienteUiState(
                        cargando = false,
                        historial = historial,
                        error = if (historial.cliente == null) {
                            "No se encontró el cliente solicitado."
                        } else {
                            null
                        }
                    )
                }
        }
    }
}