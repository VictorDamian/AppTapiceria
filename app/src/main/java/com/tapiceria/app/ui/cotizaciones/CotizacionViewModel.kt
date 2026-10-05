
package com.tapiceria.app.ui.cotizaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tapiceria.app.data.local.entity.CotizacionEntity
import com.tapiceria.app.domain.model.AtencionListado
import com.tapiceria.app.domain.repository.AtencionRepository
import com.tapiceria.app.domain.repository.CotizacionRepository
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
import java.util.Date
import java.util.Locale

/**
 * Administra el registro y seguimiento de cotizaciones.
 */
class CotizacionViewModel(
    private val cotizacionRepository: CotizacionRepository,
    private val atencionRepository: AtencionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CotizacionUiState())
    val uiState: StateFlow<CotizacionUiState> = _uiState.asStateFlow()

    init {
        observarCotizaciones()
        observarAtenciones()
    }

    private fun observarCotizaciones() {
        viewModelScope.launch {
            try {
                // Actualiza los estados vencidos antes de observar la lista.
                cotizacionRepository.marcarVencidas(System.currentTimeMillis())
            } catch (_: Exception) {
                // La consulta principal informará si existe un problema.
            }

            cotizacionRepository.observarTodas()
                .catch {
                    _uiState.update { estado ->
                        estado.copy(
                            cargando = false,
                            error = "No fue posible cargar las cotizaciones."
                        )
                    }
                }
                .collect { cotizaciones ->
                    _uiState.update {
                        it.copy(
                            cotizaciones = cotizaciones,
                            cargando = false
                        )
                    }
                }
        }
    }

    private fun observarAtenciones() {
        viewModelScope.launch {
            atencionRepository.observarPorTipo("COTIZACION")
                .catch {
                    _uiState.update { estado ->
                        estado.copy(
                            error = "No fue posible cargar las solicitudes."
                        )
                    }
                }
                .collect { atenciones ->
                    _uiState.update { estado ->
                        val idActual = estado.atencionSeleccionadaId
                        val seleccionValida = atenciones.any {
                            it.id == idActual
                        }

                        estado.copy(
                            atenciones = atenciones,
                            atencionSeleccionadaId = if (seleccionValida) {
                                idActual
                            } else {
                                atenciones.firstOrNull()?.id
                            }
                        )
                    }
                }
        }
    }

    fun seleccionarAtencion(id: Long) {
        _uiState.update {
            it.copy(
                atencionSeleccionadaId = id,
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
        // Permite solamente dígitos y un separador decimal.
        val importeNormalizado = valor.replace(',', '.')
        val formatoValido = importeNormalizado.isEmpty() ||
                importeNormalizado.matches(Regex("^\\d{0,9}(\\.\\d{0,2})?$"))

        if (formatoValido) {
            _uiState.update {
                it.copy(
                    importe = importeNormalizado,
                    error = null,
                    mensaje = null
                )
            }
        }
    }

    fun cambiarFechaVigencia(valor: String) {
        _uiState.update {
            it.copy(
                fechaVigencia = valor,
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Registra una cotización asociada a una solicitud existente.
     */
    fun guardarCotizacion() {
        val estado = _uiState.value

        if (estado.guardando) return

        val atencionId = estado.atencionSeleccionadaId

        if (atencionId == null) {
            mostrarError("Selecciona una solicitud.")
            return
        }

        val atencion = estado.atenciones.firstOrNull {
            it.id == atencionId
        }

        if (atencion == null) {
            mostrarError("La atención seleccionada ya no existe.")
            return
        }

        /*
         * Una cotización requiere cliente porque posteriormente
         * puede convertirse en un trabajo.
         */
        if (atencion.clienteId == null) {
            mostrarError(
                "La atención debe tener un cliente antes de generar una cotización."
            )
            return
        }

        val descripcion = estado.descripcion.trim()

        if (descripcion.isBlank()) {
            mostrarError("La descripción de la cotización es obligatoria.")
            return
        }

        val importeDecimal = estado.importe.toBigDecimalOrNull()

        if (importeDecimal == null || importeDecimal <= BigDecimal.ZERO) {
            mostrarError("Ingresa un importe mayor que cero.")
            return
        }

        val importeCentavos = try {
            importeDecimal
                .setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
                .longValueExact()
        } catch (_: ArithmeticException) {
            mostrarError("El importe no es válido.")
            return
        }

        val fechaVigencia = if (estado.fechaVigencia.isBlank()) {
            null
        } else {
            convertirFechaFinDia(estado.fechaVigencia)
                ?: run {
                    mostrarError(
                        "La fecha de vigencia no es válida."
                    )
                    return
                }
        }

        if (
            fechaVigencia != null &&
            fechaVigencia < System.currentTimeMillis()
        ) {
            mostrarError(
                "La fecha de vigencia debe ser hoy o posterior."
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
                /*
                 * Si estamos editando, se obtiene la cotización existente
                 * para conservar folio y fecha de creación.
                 */
                val existente =
                    estado.cotizacionEditandoId?.let {
                        cotizacionRepository.obtenerPorId(it)
                    }

                /*
                 * Para una cotización nueva se verifica que la atención
                 * todavía no tenga una cotización.
                 */
                if (existente == null) {
                    val cotizacionExistente =
                        cotizacionRepository.obtenerPorAtencion(atencionId)

                    if (cotizacionExistente != null) {
                        mostrarError(
                            "Esta atención ya tiene una cotización registrada."
                        )
                        return@launch
                    }
                }

                val cotizacion = CotizacionEntity(
                    id = existente?.id ?: 0L,

                    atencionId = atencionId,

                    // Se conserva el folio al editar.
                    folio = existente?.folio
                        ?: "COT-${System.currentTimeMillis()}",

                    descripcion = descripcion,

                    importeCentavos = importeCentavos,

                    // También se conserva la fecha original.
                    fechaCreacion =
                        existente?.fechaCreacion
                            ?: System.currentTimeMillis(),

                    fechaVigencia = fechaVigencia,

                    // Al editar se conserva el estado.
                    estado = existente?.estado ?: "PENDIENTE"
                )

                if (existente == null) {
                    cotizacionRepository.insertar(cotizacion)

                    _uiState.update {
                        it.copy(
                            guardando = false,
                            cotizacionEditandoId = null,
                            descripcion = "",
                            importe = "",
                            fechaVigencia = "",
                            mensaje =
                                "Cotización registrada: ${cotizacion.folio}"
                        )
                    }
                } else {
                    cotizacionRepository.actualizar(cotizacion)

                    _uiState.update {
                        it.copy(
                            guardando = false,
                            cotizacionEditandoId = null,
                            descripcion = "",
                            importe = "",
                            fechaVigencia = "",
                            mensaje =
                                "Cotización ${cotizacion.folio} actualizada."
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        error = "No fue posible guardar la cotización."
                    )
                }
            }
        }
    }

    fun cambiarBusquedaAtencion(valor: String) {
        _uiState.update {
            it.copy(textoBusquedaAtencion = valor)
        }
    }

    fun editarCotizacion(id: Long) {
        viewModelScope.launch {
            try {
                val cotizacion =
                    cotizacionRepository.obtenerPorId(id)
                        ?: run {
                            mostrarError("No se encontró la cotización.")
                            return@launch
                        }

                val fecha =
                    cotizacion.fechaVigencia?.let {
                        SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.ROOT
                        ).format(Date(it))
                    }.orEmpty()

                _uiState.update {
                    it.copy(
                        cotizacionEditandoId = cotizacion.id,
                        atencionSeleccionadaId = cotizacion.atencionId,
                        descripcion = cotizacion.descripcion,
                        importe =
                            BigDecimal(cotizacion.importeCentavos)
                                .movePointLeft(2)
                                .toPlainString(),
                        fechaVigencia = fecha,
                        textoBusquedaAtencion = "",
                        error = null,
                        mensaje = null
                    )
                }
            } catch (_: Exception) {
                mostrarError("No fue posible cargar la cotización.")
            }
        }
    }

    fun cancelarEdicion() {
        _uiState.update {
            it.copy(
                cotizacionEditandoId = null,
                atencionSeleccionadaId = null,
                textoBusquedaAtencion = "",
                descripcion = "",
                importe = "",
                fechaVigencia = "",
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Cambia el estado únicamente si la cotización sigue pendiente.
     */
    fun cambiarEstado(id: Long, nuevoEstado: String) {
        if (nuevoEstado !in listOf("ACEPTADA", "RECHAZADA")) return

        viewModelScope.launch {
            try {
                val cotizacion = cotizacionRepository.obtenerPorId(id)

                if (cotizacion == null) {
                    mostrarError("No se encontró la cotización.")
                    return@launch
                }

                if (cotizacion.estado != "PENDIENTE") {
                    mostrarError("Solo se pueden modificar cotizaciones pendientes.")
                    return@launch
                }

                // Comprueba la vigencia antes de aceptar o rechazar.
                if (cotizacion.fechaVigencia != null &&
                    cotizacion.fechaVigencia < System.currentTimeMillis()
                ) {
                    cotizacionRepository.marcarVencidas(
                        System.currentTimeMillis()
                    )
                    mostrarError("La cotización ya venció.")
                    return@launch
                }

                cotizacionRepository.actualizar(
                    cotizacion.copy(estado = nuevoEstado)
                )

                _uiState.update {
                    it.copy(mensaje = "Estado actualizado correctamente.")
                }
            } catch (_: Exception) {
                mostrarError("No fue posible actualizar el estado.")
            }
        }
    }

    /**
     * Convierte AAAA-MM-DD al final del día local, para que la cotización
     * continúe vigente durante toda la fecha indicada.
     */
    private fun convertirFechaFinDia(valor: String): Long? {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
            isLenient = false
        }

        val posicion = ParsePosition(0)
        val fecha = formato.parse(valor, posicion)

        // Rechaza fechas imposibles y texto adicional al final.
        if (fecha == null || posicion.index != valor.length) return null

        val calendario = Calendar.getInstance().apply {
            time = fecha
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        return calendario.timeInMillis
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