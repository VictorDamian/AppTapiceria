
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
        if (atencionId == null ||
            estado.atenciones.none { it.id == atencionId }
        ) {
            mostrarError("Primero registra una atención de tipo Cotización.")
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
                        "La vigencia debe usar el formato AAAA-MM-DD, " +
                                "por ejemplo 2026-12-31."
                    )
                    return
                }
        }

        if (fechaVigencia != null &&
            fechaVigencia < System.currentTimeMillis()
        ) {
            mostrarError("La fecha de vigencia debe ser hoy o posterior.")
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
                // El folio se genera automáticamente para evitar capturas
                // manuales repetidas. Más adelante podremos personalizarlo.
                val folio = "COT-${System.currentTimeMillis()}"

                val cotizacion = CotizacionEntity(
                    atencionId = atencionId,
                    folio = folio,
                    descripcion = descripcion,
                    importeCentavos = importeCentavos,
                    fechaVigencia = fechaVigencia
                )

                cotizacionRepository.insertar(cotizacion)

                _uiState.update {
                    it.copy(
                        descripcion = "",
                        importe = "",
                        fechaVigencia = "",
                        guardando = false,
                        mensaje = "Cotización registrada: $folio"
                    )
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