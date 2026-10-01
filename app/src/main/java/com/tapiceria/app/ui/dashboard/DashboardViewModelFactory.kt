
package com.tapiceria.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tapiceria.app.domain.repository.DashboardRepository

/**
 * Construye el ViewModel del Dashboard.
 */
class DashboardViewModelFactory(
    private val dashboardRepository: DashboardRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(dashboardRepository) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}