
package com.tapiceria.app.ui.trabajos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tapiceria.app.data.local.entity.TrabajoEntity
import com.tapiceria.app.domain.repository.ClienteRepository
import com.tapiceria.app.domain.repository.PagoRepository
import com.tapiceria.app.domain.repository.TrabajoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Gestiona el registro y seguimiento de los trabajos.
 */
class TrabajoViewModel(
    private val trabajoRepository: TrabajoRepository,
    private val clienteRepository: ClienteRepository,
    private val pagoRepository: PagoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrabajoUiState())
    val uiState: StateFlow<TrabajoUiState> = _uiState.asStateFlow()

    private val estadosPermitidos = setOf(
        "PENDIENTE",
        "EN_PROCESO",
        "TERMINADO",
        "ENTREGADO",
        "CANCELADO"
    )

    init {
        observarTrabajos()
        observarClientes()
        observarCotizaciones()
    }

    private fun observarTrabajos() {
        viewModelScope.launch {
            trabajoRepository.observarTodos()
                .catch {
                    _uiState.update { estado ->
                        estado.copy(
                            cargando = false,
                            error = "No fue posible cargar los trabajos."
                        )
                    }
                }
                .collect { trabajos ->
                    _uiState.update {
                        it.copy(trabajos = trabajos, cargando = false)
                    }
                }
        }
    }

    private fun observarClientes() {
        viewModelScope.launch {
            clienteRepository.observarTodos()
                .catch {
                    mostrarError("No fue posible cargar los clientes.")
                }
                .collect { clientes ->
                    _uiState.update { estado ->
                        val actual = estado.clienteSeleccionadoId
                        val existe = clientes.any { it.id == actual }

                        estado.copy(
                            clientes = clientes,
                            clienteSeleccionadoId = if (existe) {
                                actual
                            } else {
                                clientes.firstOrNull()?.id
                            }
                        )
                    }
                }
        }
    }

    private fun observarCotizaciones() {
        viewModelScope.launch {
            trabajoRepository.observarCotizacionesAceptadas()
                .catch {
                    mostrarError("No fue posible cargar las cotizaciones.")
                }
                .collect { cotizaciones ->
                    _uiState.update { estado ->
                        val idActual = estado.cotizacionSeleccionadaId
                        val seleccionValida = cotizaciones.any {
                            it.id == idActual &&
                                    it.clienteId == estado.clienteSeleccionadoId
                        }

                        estado.copy(
                            cotizaciones = cotizaciones,
                            cotizacionSeleccionadaId = if (seleccionValida) {
                                idActual
                            } else {
                                null
                            }
                        )
                    }
                }
        }
    }

    fun seleccionarCliente(id: Long) {
        _uiState.update {
            it.copy(
                clienteSeleccionadoId = id,
                cotizacionSeleccionadaId = null,
                error = null,
                mensaje = null
            )
        }
    }

    fun seleccionarCotizacion(id: Long?) {
        val estado = _uiState.value
        val cotizacion = estado.cotizaciones.firstOrNull {
            it.id == id && it.clienteId == estado.clienteSeleccionadoId
        }

        _uiState.update {
            it.copy(
                cotizacionSeleccionadaId = cotizacion?.id,
                // Si se selecciona una cotización, toma sus datos iniciales.
                descripcion = cotizacion?.descripcion ?: it.descripcion,
                importe = cotizacion?.let {
                    BigDecimal.valueOf(it.importeCentavos, 2)
                        .setScale(2)
                        .toPlainString()
                } ?: it.importe,
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

    fun cambiarImporte(valor: String) {
        val normalizado = valor.replace(',', '.')
        if (normalizado.isEmpty() ||
            normalizado.matches(Regex("^\\d{0,9}(\\.\\d{0,2})?$"))
        ) {
            _uiState.update {
                it.copy(importe = normalizado, error = null, mensaje = null)
            }
        }
    }

    fun cambiarFechaEntrega(valor: String) {
        _uiState.update {
            it.copy(
                fechaEntregaEstimada = valor,
                error = null,
                mensaje = null
            )
        }
    }

    fun cambiarNotas(valor: String) {
        _uiState.update {
            it.copy(notas = valor, error = null, mensaje = null)
        }
    }

    /**
     * Registra el trabajo con validaciones básicas.
     */
    fun guardarTrabajo() {
        val estado = _uiState.value

        if (estado.guardando) return

        val clienteId = estado.clienteSeleccionadoId
        if (clienteId == null ||
            estado.clientes.none { it.id == clienteId }
        ) {
            mostrarError("Selecciona un cliente activo.")
            return
        }

        val cotizacion = estado.cotizaciones.firstOrNull {
            it.id == estado.cotizacionSeleccionadaId &&
                    it.clienteId == clienteId
        }

        if (estado.cotizacionSeleccionadaId != null && cotizacion == null) {
            mostrarError("La cotización seleccionada ya no está disponible.")
            return
        }

        val descripcion = estado.descripcion.trim()
        if (descripcion.isBlank()) {
            mostrarError("La descripción del trabajo es obligatoria.")
            return
        }

        // Una cotización aceptada define el importe del trabajo.
        val importeCentavos = if (cotizacion != null) {
            cotizacion.importeCentavos
        } else {
            convertirImporteCentavos(estado.importe)
                ?: run {
                    mostrarError("Ingresa un importe válido mayor que cero.")
                    return
                }
        }

        val fechaEntrega = if (estado.fechaEntregaEstimada.isBlank()) {
            null
        } else {
            convertirFechaFinDia(estado.fechaEntregaEstimada)
                ?: run {
                    mostrarError("Usa el formato de fecha AAAA-MM-DD.")
                    return
                }
        }

        if (fechaEntrega != null &&
            fechaEntrega < inicioDelDia(System.currentTimeMillis())
        ) {
            mostrarError("La fecha de entrega debe ser hoy o posterior.")
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(guardando = true, error = null, mensaje = null)
            }

            try {
                val trabajo = TrabajoEntity(
                    clienteId = clienteId,
                    cotizacionId = cotizacion?.id,
                    folio = "TRAB-${System.currentTimeMillis()}",
                    descripcion = descripcion,
                    importeCentavos = importeCentavos,
                    fechaEntregaEstimada = fechaEntrega,
                    notas = estado.notas.trim()
                )

                val folio = trabajo.folio
                trabajoRepository.insertar(trabajo)

                _uiState.update {
                    it.copy(
                        descripcion = "",
                        importe = "",
                        fechaEntregaEstimada = "",
                        notas = "",
                        cotizacionSeleccionadaId = null,
                        guardando = false,
                        mensaje = "Trabajo registrado: $folio"
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        error = "No fue posible guardar el trabajo."
                    )
                }
            }
        }
    }

    /**
     * Cambia el estado del trabajo.
     *
     * Reglas:
     * - ENTREGADO es un estado final.
     * - CANCELADO puede revertirse posteriormente.
     * - Para entregar primero debe estar TERMINADO.
     * - Cancelar no elimina los pagos existentes.
     */
    fun cambiarEstado(
        id: Long,
        nuevoEstado: String
    ) {
        if (nuevoEstado !in estadosPermitidos) {
            mostrarError("El estado seleccionado no es válido.")
            return
        }

        viewModelScope.launch {
            try {
                val trabajo = trabajoRepository.obtenerPorId(id)

                if (trabajo == null) {
                    mostrarError("No se encontró el trabajo.")
                    return@launch
                }

                /*
                 * ENTREGADO sí es definitivo.
                 *
                 * CANCELADO NO es definitivo porque el requerimiento permite
                 * volver posteriormente a otro estado.
                 */
                if (trabajo.estado == "ENTREGADO") {
                    mostrarError(
                        "Un trabajo entregado no puede cambiar de estado."
                    )
                    return@launch
                }

                /*
                 * Para marcar como ENTREGADO primero debe pasar por TERMINADO.
                 */
                if (
                    nuevoEstado == "ENTREGADO" &&
                    trabajo.estado != "TERMINADO"
                ) {
                    mostrarError(
                        "Primero marca el trabajo como terminado."
                    )
                    return@launch
                }

                val actualizado = trabajo.copy(
                    estado = nuevoEstado,

                    /*
                     * La fecha real solamente se registra al entregar.
                     *
                     * Si regresamos desde CANCELADO a otro estado,
                     * conservamos la fecha existente porque no corresponde
                     * modificarla hasta que realmente se entregue.
                     */
                    fechaEntregaReal =
                        if (nuevoEstado == "ENTREGADO") {
                            System.currentTimeMillis()
                        } else {
                            trabajo.fechaEntregaReal
                        }
                )

                trabajoRepository.actualizar(actualizado)

                _uiState.update {
                    it.copy(
                        mensaje = when (nuevoEstado) {
                            "CANCELADO" ->
                                "Trabajo cancelado correctamente."

                            else ->
                                "Estado del trabajo actualizado."
                        }
                    )
                }

            } catch (_: Exception) {
                mostrarError(
                    "No fue posible actualizar el estado del trabajo."
                )
            }
        }
    }

    /**
     * Obtiene el total que ya ha sido pagado de un trabajo.
     *
     * Se consulta directamente al repositorio para trabajar con el valor
     * actual de la base de datos y no con información potencialmente antigua
     * de la interfaz.
     */
    private suspend fun obtenerTotalPagado(
        trabajoId: Long
    ): Long {
        return pagoRepository.obtenerTotalPagado(trabajoId)
    }

    private fun convertirImporteCentavos(valor: String): Long? {
        return try {
            val decimal = valor.toBigDecimalOrNull() ?: return null

            if (decimal <= BigDecimal.ZERO) return null

            decimal
                .setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
                .longValueExact()
        } catch (_: ArithmeticException) {
            null
        }
    }

    private fun convertirFechaFinDia(valor: String): Long? {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
            isLenient = false
        }

        val posicion = ParsePosition(0)
        val fecha = formato.parse(valor, posicion)

        if (fecha == null || posicion.index != valor.length) return null

        return Calendar.getInstance().apply {
            time = fecha
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private fun inicioDelDia(fecha: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = fecha
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
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