
package com.tapiceria.app.ui.atenciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tapiceria.app.domain.repository.AtencionRepository
import com.tapiceria.app.domain.repository.ClienteRepository

/**
 * Construye el ViewModel con los repositorios de la aplicación.
 */
class AtencionViewModelFactory(
    private val atencionRepository: AtencionRepository,
    private val clienteRepository: ClienteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AtencionViewModel::class.java)) {
            "ViewModel no soportado: ${modelClass.name}"
        }

        return AtencionViewModel(
            atencionRepository = atencionRepository,
            clienteRepository = clienteRepository
        ) as T
    }
}