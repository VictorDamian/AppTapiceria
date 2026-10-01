
package com.tapiceria.app.ui.pagos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tapiceria.app.domain.repository.PagoRepository
import com.tapiceria.app.domain.repository.TrabajoRepository

/**
 * Construye el ViewModel con sus repositorios.
 */
class PagoViewModelFactory(
    private val trabajoRepository: TrabajoRepository,
    private val pagoRepository: PagoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PagoViewModel::class.java)) {
            return PagoViewModel(
                trabajoRepository = trabajoRepository,
                pagoRepository = pagoRepository
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}