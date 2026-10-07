package com.tapiceria.app.ui.cotizaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tapiceria.app.domain.repository.AtencionRepository
import com.tapiceria.app.domain.repository.CotizacionRepository
import com.tapiceria.app.domain.repository.TrabajoRepository

/**
 * Proporciona las dependencias necesarias al ViewModel.
 */
class CotizacionViewModelFactory(
    private val cotizacionRepository: CotizacionRepository,
    private val atencionRepository: AtencionRepository,
    private val trabajoRepository: TrabajoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        require(
            modelClass.isAssignableFrom(
                CotizacionViewModel::class.java
            )
        ) {
            "ViewModel no soportado: ${modelClass.name}"
        }

        return CotizacionViewModel(
            cotizacionRepository = cotizacionRepository,
            atencionRepository = atencionRepository,
            trabajoRepository = trabajoRepository
        ) as T
    }
}