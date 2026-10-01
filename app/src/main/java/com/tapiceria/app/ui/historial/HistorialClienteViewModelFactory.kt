package com.tapiceria.app.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tapiceria.app.domain.repository.HistorialClienteRepository

/**
 * Construye el ViewModel con el identificador y repositorio requeridos.
 */
class HistorialClienteViewModelFactory(
    private val clienteId: Long,
    private val repository: HistorialClienteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(
            modelClass.isAssignableFrom(HistorialClienteViewModel::class.java)
        ) {
            "ViewModel no soportado: ${modelClass.name}"
        }

        return HistorialClienteViewModel(
            clienteId = clienteId,
            repository = repository
        ) as T
    }
}