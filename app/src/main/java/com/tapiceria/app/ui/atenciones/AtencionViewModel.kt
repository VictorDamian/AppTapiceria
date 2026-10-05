
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
                .catch {
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
     * Observa todos los clientes.
     *
     * Se utilizan todos y no solamente los activos porque una
     * atención histórica puede estar asociada a un cliente
     * que posteriormente fue desactivado.
     */
    private fun observarClientes() {
        viewModelScope.launch {
            clienteRepository.observarTodos()
                .catch {
                    _uiState.update { estado ->
                        estado.copy(
                            error = "No fue posible cargar los clientes."
                        )
                    }
                }
                .collect { clientes ->
                    _uiState.update {
                        it.copy(clientes = clientes)
                    }
                }
        }
    }

    /**
     * Cambia el texto de búsqueda de clientes.
     */
    fun cambiarBusquedaCliente(texto: String) {
        _uiState.update {
            it.copy(
                textoBusquedaCliente = texto,
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Selecciona un cliente.
     *
     * Solamente se utiliza para clientes activos.
     */
    fun seleccionarCliente(clienteId: Long) {
        val cliente = _uiState.value.clientes
            .firstOrNull { it.id == clienteId }

        if (cliente == null) {
            mostrarError("El cliente seleccionado no existe.")
            return
        }

        if (!cliente.activo) {
            mostrarError(
                "El cliente está inactivo. Activa el cliente para seleccionarlo."
            )
            return
        }

        _uiState.update {
            it.copy(
                clienteSeleccionadoId = clienteId,
                textoBusquedaCliente = "",
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Permite dejar explícitamente la atención sin cliente.
     */
    fun seleccionarSinCliente() {
        _uiState.update {
            it.copy(
                clienteSeleccionadoId = null,
                textoBusquedaCliente = "",
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Cambia el tipo de atención.
     */
    fun seleccionarTipo(tipo: String) {

        if (tipo != "CONSULTA" && tipo != "COTIZACION") {
            return
        }

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
     * Carga una atención existente en el formulario.
     */
    fun editarAtencion(id: Long) {

        if (_uiState.value.guardando) {
            return
        }

        viewModelScope.launch {

            try {

                val atencion =
                    atencionRepository.obtenerPorId(id)

                if (atencion == null) {
                    mostrarError(
                        "No se encontró la atención seleccionada."
                    )
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        atencionEditandoId = atencion.id,
                        clienteSeleccionadoId = atencion.clienteId,
                        textoBusquedaCliente = "",
                        tipoSeleccionado = atencion.tipo,
                        descripcion = atencion.descripcion,
                        notas = atencion.notas,
                        error = null,
                        mensaje = null
                    )
                }

            } catch (ex: Exception) {

                mostrarError(
                    "No fue posible cargar la atención."
                )
            }
        }
    }

    /**
     * Cancela la edición y devuelve el formulario al modo
     * de nueva atención.
     */
    fun cancelarEdicion() {

        _uiState.update {
            it.copy(
                atencionEditandoId = null,
                clienteSeleccionadoId = null,
                textoBusquedaCliente = "",
                tipoSeleccionado = "CONSULTA",
                descripcion = "",
                notas = "",
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Valida el formulario y registra una nueva atención.
     * Inserta una atención nueva o actualiza una existente.
     */
    fun guardarAtencion() {
        val estado = _uiState.value

        // Evita guardar registros incompletos o duplicar clics.
        if (estado.guardando) return

        val descripcion = estado.descripcion.trim()

        if (descripcion.isBlank()) {
            mostrarError("La descripción es obligatoria.")
            return
        }

        if (estado.tipoSeleccionado !in listOf("CONSULTA", "COTIZACION")) {
            mostrarError("Selecciona un tipo de atención válido.")
            return
        }

        // Si existe cliente, debe seguir existiendo.
        val clienteSeleccionado =
            estado.clienteSeleccionadoId?.let { id ->
                estado.clientes.firstOrNull {
                    it.id == id
                }
            }

        if (
            estado.clienteSeleccionadoId != null &&
            clienteSeleccionado == null
        ) {
            mostrarError(
                "El cliente seleccionado ya no existe."
            )
            return
        }

        // Una nueva atención solamente puede seleccionar
        // clientes activos.
        if (
            estado.atencionEditandoId == null &&
            clienteSeleccionado != null &&
            !clienteSeleccionado.activo
        ) {
            mostrarError(
                "No puedes registrar una nueva atención para un cliente inactivo."
            )
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
                    id = estado.atencionEditandoId ?: 0L,
                    clienteId = estado.clienteSeleccionadoId,
                    tipo = estado.tipoSeleccionado,
                    descripcion = descripcion,
                    notas = estado.notas.trim()
                )

                if (estado.atencionEditandoId == null) {

                    atencionRepository.insertar(atencion)

                    _uiState.update {
                        it.copy(
                            clienteSeleccionadoId = null,
                            textoBusquedaCliente = "",
                            descripcion = "",
                            notas = "",
                            guardando = false,
                            mensaje = "Atención registrada correctamente."
                        )
                    }

                } else {

                    atencionRepository.actualizar(atencion)

                    _uiState.update {
                        it.copy(
                            atencionEditandoId = null,
                            clienteSeleccionadoId = null,
                            textoBusquedaCliente = "",
                            descripcion = "",
                            notas = "",
                            guardando = false,
                            mensaje = "Atención actualizada correctamente."
                        )
                    }
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