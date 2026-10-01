
package com.tapiceria.app.ui.trabajos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tapiceria.app.domain.repository.ClienteRepository
import com.tapiceria.app.domain.repository.TrabajoRepository

/**
 * Inyecta los repositorios requeridos por TrabajoViewModel.
 */
class TrabajoViewModelFactory(
    private val trabajoRepository: TrabajoRepository,
    private val clienteRepository: ClienteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(TrabajoViewModel::class.java)) {
            "ViewModel no soportado: ${modelClass.name}"
        }

        return TrabajoViewModel(
            trabajoRepository = trabajoRepository,
            clienteRepository = clienteRepository
        ) as T
    }
}