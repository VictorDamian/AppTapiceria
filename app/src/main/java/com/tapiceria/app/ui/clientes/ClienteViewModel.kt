package com.tapiceria.app.ui.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tapiceria.app.data.local.entity.ClienteEntity
import com.tapiceria.app.domain.repository.ClienteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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

    /**
     * Observa todos los clientes.
     *
     * Esto permite administrar también los clientes desactivados.
     */
    private fun observarClientes() {
        viewModelScope.launch {
            try {
                repository.observarTodos().collect { clientes ->
                    _uiState.update {
                        it.copy(
                            clientes = clientes,
                            cargando = false
                        )
                    }
                }
            } catch (ex: CancellationException) {
                // La cancelación de la corrutina debe propagarse.
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
     * Guarda un cliente nuevo o actualiza uno existente.
     *
     * Cuando se edita se conserva el valor actual de activo.
     */
    fun guardarCliente(
        id: Long = 0,
        nombre: String,
        telefono: String,
        direccion: String,
        notas: String
    ) {
        // Evita guardar dos veces si el usuario presiona rápidamente.
        if (_uiState.value.guardando) {
            return
        }

        val nombreLimpio = nombre.trim()
        val telefonoLimpio = telefono.trim()

        // El nombre es obligatorio.
        if (nombreLimpio.isBlank()) {
            _uiState.update {
                it.copy(error = "El nombre del cliente es obligatorio.")
            }
            return
        }

        // Valida el teléfono únicamente cuando se proporcionó.
        if (
            telefonoLimpio.isNotEmpty() &&
            !telefonoLimpio.matches(
                Regex("^[0-9+()\\-\\s]{7,20}$")
            )
        ) {
            _uiState.update {
                it.copy(error = "Revisa el formato del teléfono.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    guardando = true,
                    error = null,
                    mensaje = null
                )
            }

            try {
                if (id == 0L) {

                    // Cliente nuevo: siempre inicia como activo.
                    val cliente = ClienteEntity(
                        nombre = nombreLimpio,
                        telefono = telefonoLimpio,
                        direccion = direccion.trim(),
                        notas = notas.trim(),
                        activo = true
                    )

                    repository.insertar(cliente)

                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mensaje = "Cliente registrado correctamente."
                        )
                    }

                } else {

                    /*
                     * Antes de actualizar obtenemos el registro actual.
                     *
                     * Esto es importante porque al reconstruir ClienteEntity
                     * no debemos cambiar accidentalmente el estado activo/inactivo.
                     */
                    val clienteActual = repository.obtenerPorId(id)

                    if (clienteActual == null) {
                        _uiState.update {
                            it.copy(
                                guardando = false,
                                error = "El cliente ya no existe."
                            )
                        }
                        return@launch
                    }

                    val clienteActualizado = clienteActual.copy(
                        nombre = nombreLimpio,
                        telefono = telefonoLimpio,
                        direccion = direccion.trim(),
                        notas = notas.trim()
                    )

                    repository.actualizar(clienteActualizado)

                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mensaje = "Cliente actualizado correctamente."
                        )
                    }
                }

            } catch (ex: CancellationException) {
                throw ex

            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        error = "No fue posible guardar el cliente."
                    )
                }
            }
        }
    }

    /**
     * Desactiva lógicamente al cliente.
     *
     * Su información histórica permanece intacta.
     */
    /**
     * Desactiva lógicamente al cliente.
     *
     * La información histórica del cliente permanece intacta.
     */
    /**
     * Desactiva lógicamente al cliente.
     *
     * Antes de hacerlo se valida que no tenga cotizaciones
     * en estados que todavía representen una operación activa.
     */
    fun desactivarCliente(id: Long) {

        if (_uiState.value.guardando) {
            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    guardando = true,
                    error = null,
                    mensaje = null
                )
            }

            try {

                /*
                 * Primero verificamos que el cliente exista.
                 */
                val cliente = repository.obtenerPorId(id)

                if (cliente == null) {

                    _uiState.update {
                        it.copy(
                            guardando = false,
                            error = "El cliente ya no existe."
                        )
                    }

                    return@launch
                }

                /*
                 * Una cotización PENDIENTE, ACEPTADA o VENCIDA
                 * todavía bloquea la baja.
                 *
                 * RECHAZADA y CANCELADA no bloquean.
                 */
                val tieneCotizacionesBloqueantes =
                    repository.tieneCotizacionesBloqueantes(id)

                if (tieneCotizacionesBloqueantes) {

                    _uiState.update {
                        it.copy(
                            guardando = false,
                            error =
                                "No se puede desactivar al cliente porque " +
                                        "tiene cotizaciones que todavía no están " +
                                        "rechazadas o canceladas."
                        )
                    }

                    return@launch
                }

                /*
                 * Si no existen cotizaciones bloqueantes,
                 * realizamos la baja lógica.
                 */
                repository.desactivar(id)

                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensaje =
                            "Cliente desactivado correctamente."
                    )
                }

            } catch (ex: CancellationException) {

                throw ex

            } catch (ex: Exception) {

                _uiState.update {
                    it.copy(
                        guardando = false,
                        error =
                            "No fue posible desactivar el cliente."
                    )
                }
            }
        }
    }

    /**
     * Reactiva un cliente previamente desactivado.
     */
    fun activarCliente(id: Long) {
        if (_uiState.value.guardando) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    guardando = true,
                    error = null,
                    mensaje = null
                )
            }

            try {
                // El repositorio actualmente devuelve Unit.
                repository.activar(id)

                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensaje = "Cliente activado correctamente."
                    )
                }

            } catch (ex: CancellationException) {
                throw ex

            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        error = "No fue posible activar el cliente."
                    )
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

    private fun mostrarError(mensaje: String) {
        _uiState.update {
            it.copy(
                error = mensaje,
                mensaje = null
            )
        }
    }
}