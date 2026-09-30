
package com.tapiceria.app.ui.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tapiceria.app.domain.repository.ClienteRepository

/**
 * Proporciona el repositorio al ViewModel.
 */
class ClienteViewModelFactory(
    private val repository: ClienteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClienteViewModel::class.java)) {
            return ClienteViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "ViewModel no soportado: ${modelClass.name}"
        )
    }
}