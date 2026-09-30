
package com.tapiceria.app.ui.atenciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tapiceria.app.data.local.entity.AtencionEntity
import com.tapiceria.app.domain.repository.AtencionRepository
import com.tapiceria.app.domain.repository.ClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Administra el estado y las operaciones de la pantalla de atenciones.
 */
class AtencionViewModel(
    private val atencionRepository: AtencionRepository,
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AtencionUiState())
    val uiState: StateFlow<AtencionUiState> = _uiState.asStateFlow()

    init {
        observarAtenciones()
        observarClientes()
    }

    /**
     * Observa las atenciones registradas en Room.
     * Los cambios se reflejan automáticamente en la pantalla.
     */
    private fun observarAtenciones() {
        viewModelScope.launch {
            atencionRepository.observarTodas()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = "No fue posible cargar las atenciones."
                        )
                    }
                }
                .collect { atenciones ->
                    _uiState.update {
                        it.copy(
                            atenciones = atenciones,
                            cargando = false
                        )
                    }
                }
        }
    }

    /**
     * Carga los clientes activos disponibles para el formulario.
     */
    private fun observarClientes() {
        viewModelScope.launch {
            clienteRepository.observarActivos()
                .catch {
                    _uiState.update { estado ->
                        estado.copy(
                            error = "No fue posible cargar los clientes."
                        )
                    }
                }
                .collect { clientes ->
                    _uiState.update { estado ->
                        val clienteActual = estado.clienteSeleccionadoId

                        // Conserva la selección si el cliente sigue activo.
                        val seleccionValida = clientes.any {
                            it.id == clienteActual
                        }

                        estado.copy(
                            clientes = clientes,
                            clienteSeleccionadoId = if (seleccionValida) {
                                clienteActual
                            } else {
                                clientes.firstOrNull()?.id
                            }
                        )
                    }
                }
        }
    }

    fun seleccionarCliente(clienteId: Long) {
        _uiState.update {
            it.copy(
                clienteSeleccionadoId = clienteId,
                error = null,
                mensaje = null
            )
        }
    }

    fun seleccionarTipo(tipo: String) {
        if (tipo != "CONSULTA" && tipo != "COTIZACION") return

        _uiState.update {
            it.copy(
                tipoSeleccionado = tipo,
                error = null,
                mensaje = null
            )
        }
    }

    fun cambiarDescripcion(valor: String) {
        _uiState.update {
            it.copy(descripcion = valor, error = null, mensaje = null)
        }
    }

    fun cambiarNotas(valor: String) {
        _uiState.update {
            it.copy(notas = valor, error = null, mensaje = null)
        }
    }

    /**
     * Valida el formulario y registra una nueva atención.
     */
    fun guardarAtencion() {
        val estado = _uiState.value

        // Evita guardar registros incompletos o duplicar clics.
        if (estado.guardando) return

        val clienteId = estado.clienteSeleccionadoId
        if (clienteId == null ||
            estado.clientes.none { it.id == clienteId }
        ) {
            mostrarError("Selecciona un cliente activo.")
            return
        }

        val descripcion = estado.descripcion.trim()
        if (descripcion.isBlank()) {
            mostrarError("La descripción es obligatoria.")
            return
        }

        if (estado.tipoSeleccionado !in listOf("CONSULTA", "COTIZACION")) {
            mostrarError("Selecciona un tipo de atención válido.")
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
                val atencion = AtencionEntity(
                    clienteId = clienteId,
                    tipo = estado.tipoSeleccionado,
                    descripcion = descripcion,
                    notas = estado.notas.trim()
                )

                atencionRepository.insertar(atencion)

                _uiState.update {
                    it.copy(
                        descripcion = "",
                        notas = "",
                        guardando = false,
                        mensaje = "Atención registrada correctamente."
                    )
                }
            } catch (ex: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        error = "No fue posible guardar la atención."
                    )
                }
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update {
            it.copy(error = null, mensaje = null)
        }
    }

    private fun mostrarError(mensaje: String) {
        _uiState.update {
            it.copy(error = mensaje, mensaje = null)
        }
    }
}