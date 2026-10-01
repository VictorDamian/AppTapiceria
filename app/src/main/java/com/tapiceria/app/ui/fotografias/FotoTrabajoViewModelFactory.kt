
package com.tapiceria.app.ui.fotografias

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tapiceria.app.domain.repository.FotoTrabajoRepository
import com.tapiceria.app.domain.repository.TrabajoRepository

/**
 * Construye el ViewModel con los repositorios y el contexto de aplicación.
 */
class FotoTrabajoViewModelFactory(
    private val context: Context,
    private val trabajoRepository: TrabajoRepository,
    private val fotoTrabajoRepository: FotoTrabajoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(
            modelClass.isAssignableFrom(FotoTrabajoViewModel::class.java)
        ) {
            "ViewModel no soportado: ${modelClass.name}"
        }

        return FotoTrabajoViewModel(
            context = context.applicationContext,
            trabajoRepository = trabajoRepository,
            fotoRepository = fotoTrabajoRepository
        ) as T
    }
}