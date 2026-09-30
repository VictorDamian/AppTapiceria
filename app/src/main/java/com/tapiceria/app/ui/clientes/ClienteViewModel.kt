package com.tapiceria.app.ui.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tapiceria.app.data.local.entity.ClienteEntity
import com.tapiceria.app.domain.repository.ClienteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Administra las operaciones de clientes y el estado de la pantalla.
 * La interfaz no accede directamente a Room.
 */
class ClienteViewModel(
    private val repository: ClienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClienteUiState())
    val uiState: StateFlow<ClienteUiState> = _uiState.asStateFlow()

    init {
        // Observa los cambios de Room durante la vida del ViewModel.
        observarClientes()
    }

    private fun observarClientes() {
        viewModelScope.launch {
            try {
                repository.observarActivos().collect { clientes ->
                    _uiState.update {
                        it.copy(clientes = clientes, cargando = false)
                    }
                }
            } catch (ex: CancellationException) {
                // Nunca se debe impedir la cancelación de una corrutina.
                throw ex
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No fue posible cargar los clientes."
                    )
                }
            }
        }
    }

    /** Actualiza el texto de búsqueda sin consultar la BD por cada tecla. */
    fun cambiarBusqueda(texto: String) {
        _uiState.update {
            it.copy(textoBusqueda = texto)
        }
    }

    /**
     * Registra o actualiza un cliente.
     * Un ID mayor que cero indica que es una edición.
     */
    fun guardarCliente(
        id: Long = 0,
        nombre: String,
        telefono: String,
        direccion: String,
        notas: String
    ) {
        val nombreLimpio = nombre.trim()
        val telefonoLimpio = telefono.trim()

        // Validaciones básicas antes de guardar.
        if (nombreLimpio.isBlank()) {
            _uiState.update {
                it.copy(error = "El nombre del cliente es obligatorio.")
            }
            return
        }

        if (telefonoLimpio.isNotEmpty() &&
            !telefonoLimpio.matches(Regex("^[0-9+()\\-\\s]{7,20}$"))
        ) {
            _uiState.update {
                it.copy(error = "Revisa el formato del teléfono.")
            }
            return
        }

        viewModelScope.launch {
            try {
                val cliente = ClienteEntity(
                    id = id,
                    nombre = nombreLimpio,
                    telefono = telefonoLimpio,
                    direccion = direccion.trim(),
                    notas = notas.trim()
                )

                if (id == 0L) {
                    repository.insertar(cliente)
                    _uiState.update {
                        it.copy(mensaje = "Cliente registrado correctamente.")
                    }
                } else {
                    repository.actualizar(cliente)
                    _uiState.update {
                        it.copy(mensaje = "Cliente actualizado correctamente.")
                    }
                }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(error = "No fue posible guardar el cliente.")
                }
            }
        }
    }

    /** Desactiva al cliente sin borrar sus trabajos ni pagos. */
    fun desactivarCliente(id: Long) {
        viewModelScope.launch {
            try {
                repository.desactivar(id)
                _uiState.update {
                    it.copy(mensaje = "Cliente desactivado correctamente.")
                }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(error = "No fue posible desactivar el cliente.")
                }
            }
        }
    }

    /** Permite que la pantalla cierre mensajes y errores ya mostrados. */
    fun limpiarMensajes() {
        _uiState.update {
            it.copy(error = null, mensaje = null)
        }
    }
}