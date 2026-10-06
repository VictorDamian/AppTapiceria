package com.tapiceria.app.ui.cotizaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tapiceria.app.data.local.entity.CotizacionEntity
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

    private val _uiState =
        MutableStateFlow(CotizacionUiState())

    val uiState: StateFlow<CotizacionUiState> =
        _uiState.asStateFlow()

    init {
        observarCotizaciones()
        observarAtenciones()
    }

    /**
     * Observa todas las cotizaciones.
     */
    private fun observarCotizaciones() {

        viewModelScope.launch {

            try {
                // Actualizamos vencimientos antes de mostrar la lista.
                cotizacionRepository.marcarVencidas(
                    System.currentTimeMillis()
                )
            } catch (_: Exception) {
                // La observación principal manejará posibles errores.
            }

            cotizacionRepository.observarTodas()
                .catch {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error =
                                "No fue posible cargar las cotizaciones."
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

    /**
     * Observa solamente las atenciones que representan
     * solicitudes de cotización.
     */
    private fun observarAtenciones() {

        viewModelScope.launch {

            atencionRepository
                .observarPorTipo("COTIZACION")
                .catch {
                    _uiState.update {
                        it.copy(
                            error =
                                "No fue posible cargar las solicitudes."
                        )
                    }
                }
                .collect { atenciones ->

                    _uiState.update { estado ->

                        val idActual =
                            estado.atencionSeleccionadaId

                        val existeSeleccion =
                            atenciones.any {
                                it.id == idActual
                            }

                        estado.copy(
                            atenciones = atenciones,

                            atencionSeleccionadaId =
                                if (existeSeleccion) {
                                    idActual
                                } else {
                                    null
                                }
                        )
                    }
                }
        }
    }

    /**
     * Actualiza el texto utilizado para buscar solicitudes.
     */
    fun cambiarBusquedaAtencion(
        texto: String
    ) {
        _uiState.update {
            it.copy(
                textoBusquedaAtencion = texto,
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Selecciona una atención.
     */
    fun seleccionarAtencion(
        id: Long
    ) {

        val atencion =
            _uiState.value.atenciones
                .firstOrNull { it.id == id }

        if (atencion == null) {
            mostrarError(
                "La solicitud seleccionada no existe."
            )
            return
        }

        // Una atención sin cliente no puede convertirse
        // directamente en una cotización.
        if (atencion.clienteId == null) {
            mostrarError(
                "La atención debe tener un cliente para generar una cotización."
            )
            return
        }

        _uiState.update {
            it.copy(
                atencionSeleccionadaId = id,
                textoBusquedaAtencion = "",
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Cambia la descripción de la cotización.
     */
    fun cambiarDescripcion(
        valor: String
    ) {
        _uiState.update {
            it.copy(
                descripcion = valor,
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Cambia el importe.
     */
    fun cambiarImporte(
        valor: String
    ) {

        val importeNormalizado =
            valor.replace(',', '.')

        val formatoValido =
            importeNormalizado.isEmpty() ||
                    importeNormalizado.matches(
                        Regex(
                            "^\\d{0,9}(\\.\\d{0,2})?$"
                        )
                    )

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

    /**
     * Cambia la fecha de vigencia.
     */
    fun cambiarFechaVigencia(
        valor: String
    ) {
        _uiState.update {
            it.copy(
                fechaVigencia = valor,
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Carga una cotización existente en el formulario.
     */
    fun editarCotizacion(
        id: Long
    ) {

        if (_uiState.value.guardando) {
            return
        }

        viewModelScope.launch {

            try {

                val cotizacion =
                    cotizacionRepository.obtenerPorId(id)

                if (cotizacion == null) {
                    mostrarError(
                        "No se encontró la cotización."
                    )
                    return@launch
                }

                val fechaTexto =
                    cotizacion.fechaVigencia?.let {
                        formatearFechaEntrada(it)
                    } ?: ""

                _uiState.update {
                    it.copy(
                        cotizacionEditandoId =
                            cotizacion.id,

                        atencionSeleccionadaId =
                            cotizacion.atencionId,

                        textoBusquedaAtencion = "",

                        descripcion =
                            cotizacion.descripcion,

                        importe =
                            formatearImporte(
                                cotizacion.importeCentavos
                            ),

                        fechaVigencia =
                            fechaTexto,

                        error = null,
                        mensaje = null
                    )
                }

            } catch (_: Exception) {

                mostrarError(
                    "No fue posible cargar la cotización."
                )
            }
        }
    }

    /**
     * Cancela la edición.
     */
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
     * Guarda una nueva cotización o actualiza una existente.
     */
    fun guardarCotizacion() {

        val estado = _uiState.value

        if (estado.guardando) {
            return
        }

        val atencionId =
            estado.atencionSeleccionadaId

        if (atencionId == null) {

            mostrarError(
                "Selecciona una solicitud de cotización."
            )

            return
        }

        val atencion =
            estado.atenciones.firstOrNull {
                it.id == atencionId
            }

        if (atencion == null) {

            mostrarError(
                "La solicitud seleccionada ya no existe."
            )

            return
        }

        /**
         * Regla de negocio:
         *
         * Una atención puede existir sin cliente,
         * pero una cotización NO.
         */
        if (atencion.clienteId == null) {

            mostrarError(
                "No se puede generar una cotización sin cliente."
            )

            return
        }

        val descripcion =
            estado.descripcion.trim()

        if (descripcion.isBlank()) {

            mostrarError(
                "La descripción de la cotización es obligatoria."
            )

            return
        }

        val importeDecimal =
            estado.importe.toBigDecimalOrNull()

        if (
            importeDecimal == null ||
            importeDecimal <= BigDecimal.ZERO
        ) {

            mostrarError(
                "Ingresa un importe mayor que cero."
            )

            return
        }

        val importeCentavos = try {

            importeDecimal
                .setScale(
                    2,
                    RoundingMode.UNNECESSARY
                )
                .movePointRight(2)
                .longValueExact()

        } catch (_: ArithmeticException) {

            mostrarError(
                "El importe no es válido."
            )

            return
        }

        val fechaVigencia =
            if (estado.fechaVigencia.isBlank()) {

                null

            } else {

                convertirFechaFinDia(
                    estado.fechaVigencia
                ) ?: run {

                    mostrarError(
                        "La fecha debe usar el formato AAAA-MM-DD."
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

                /**
                 * Comprobamos que no exista otra cotización
                 * para la misma atención.
                 *
                 * Durante edición se permite encontrar la
                 * propia cotización que estamos modificando.
                 */
                val cotizacionExistente =
                    cotizacionRepository
                        .obtenerPorAtencion(atencionId)

                if (
                    cotizacionExistente != null &&
                    cotizacionExistente.id !=
                    estado.cotizacionEditandoId
                ) {

                    _uiState.update {
                        it.copy(
                            guardando = false,
                            error =
                                "Esta atención ya tiene una cotización registrada."
                        )
                    }

                    return@launch
                }

                if (
                    estado.cotizacionEditandoId == null
                ) {

                    // Alta: generamos un folio nuevo.
                    val folio =
                        "COT-${System.currentTimeMillis()}"

                    val cotizacion =
                        CotizacionEntity(
                            atencionId = atencionId,
                            folio = folio,
                            descripcion = descripcion,
                            importeCentavos =
                                importeCentavos,
                            fechaVigencia =
                                fechaVigencia
                        )

                    cotizacionRepository.insertar(
                        cotizacion
                    )

                    _uiState.update {
                        it.copy(
                            atencionSeleccionadaId = null,
                            textoBusquedaAtencion = "",
                            descripcion = "",
                            importe = "",
                            fechaVigencia = "",
                            guardando = false,
                            mensaje =
                                "Cotización registrada: $folio"
                        )
                    }

                } else {

                    /**
                     * Edición:
                     * recuperamos nuevamente la entidad para
                     * conservar campos que no deben cambiar,
                     * principalmente el folio y fecha de creación.
                     */
                    val existente =
                        cotizacionRepository.obtenerPorId(
                            estado.cotizacionEditandoId
                        )

                    if (existente == null) {

                        _uiState.update {
                            it.copy(
                                guardando = false,
                                error =
                                    "La cotización ya no existe."
                            )
                        }

                        return@launch
                    }

                    /**
                     * No permitimos modificar una cotización
                     * que ya no está pendiente.
                     *
                     * Esto conserva la regla existente:
                     * ACEPTADA, RECHAZADA y VENCIDA son estados
                     * históricos.
                     */
                    if (existente.estado != "PENDIENTE") {

                        _uiState.update {
                            it.copy(
                                guardando = false,
                                error =
                                    "Solo se pueden editar cotizaciones pendientes."
                            )
                        }

                        return@launch
                    }

                    val actualizada =
                        existente.copy(
                            atencionId = atencionId,
                            descripcion = descripcion,
                            importeCentavos =
                                importeCentavos,
                            fechaVigencia =
                                fechaVigencia
                            // folio se conserva.
                            // fechaCreacion se conserva.
                            // estado se conserva.
                        )

                    cotizacionRepository.actualizar(
                        actualizada
                    )

                    _uiState.update {
                        it.copy(
                            cotizacionEditandoId = null,
                            atencionSeleccionadaId = null,
                            textoBusquedaAtencion = "",
                            descripcion = "",
                            importe = "",
                            fechaVigencia = "",
                            guardando = false,
                            mensaje =
                                "Cotización actualizada correctamente."
                        )
                    }
                }

            } catch (_: Exception) {

                _uiState.update {
                    it.copy(
                        guardando = false,
                        error =
                            "No fue posible guardar la cotización."
                    )
                }
            }
        }
    }

    /**
     * Cambia el estado de una cotización pendiente.
     */
    fun cambiarEstado(
        id: Long,
        nuevoEstado: String
    ) {

        if (
            nuevoEstado != "ACEPTADA" &&
            nuevoEstado != "RECHAZADA"
        ) {
            return
        }

        viewModelScope.launch {

            try {

                val cotizacion =
                    cotizacionRepository.obtenerPorId(id)

                if (cotizacion == null) {

                    mostrarError(
                        "No se encontró la cotización."
                    )

                    return@launch
                }

                if (cotizacion.estado != "PENDIENTE") {

                    mostrarError(
                        "Solo se pueden modificar cotizaciones pendientes."
                    )

                    return@launch
                }

                if (
                    cotizacion.fechaVigencia != null &&
                    cotizacion.fechaVigencia <
                    System.currentTimeMillis()
                ) {

                    cotizacionRepository.marcarVencidas(
                        System.currentTimeMillis()
                    )

                    mostrarError(
                        "La cotización ya venció."
                    )

                    return@launch
                }

                cotizacionRepository.actualizar(
                    cotizacion.copy(
                        estado = nuevoEstado
                    )
                )

                _uiState.update {
                    it.copy(
                        mensaje =
                            "Estado actualizado correctamente."
                    )
                }

            } catch (_: Exception) {

                mostrarError(
                    "No fue posible actualizar el estado."
                )
            }
        }
    }

    /**
     * Convierte una fecha AAAA-MM-DD al final de ese día.
     */
    private fun convertirFechaFinDia(
        valor: String
    ): Long? {

        val formato =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.ROOT
            ).apply {
                isLenient = false
            }

        val posicion =
            ParsePosition(0)

        val fecha =
            formato.parse(
                valor,
                posicion
            )

        if (
            fecha == null ||
            posicion.index != valor.length
        ) {
            return null
        }

        val calendario =
            Calendar.getInstance().apply {

                time = fecha

                set(
                    Calendar.HOUR_OF_DAY,
                    23
                )

                set(
                    Calendar.MINUTE,
                    59
                )

                set(
                    Calendar.SECOND,
                    59
                )

                set(
                    Calendar.MILLISECOND,
                    999
                )
            }

        return calendario.timeInMillis
    }

    /**
     * Convierte una fecha almacenada a texto
     * para mostrarla en el formulario.
     */
    private fun formatearFechaEntrada(
        fecha: Long
    ): String {

        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.ROOT
        ).format(fecha)
    }

    /**
     * Convierte centavos a texto decimal.
     */
    private fun formatearImporte(
        centavos: Long
    ): String {

        val importe =
            BigDecimal.valueOf(centavos)
                .movePointLeft(2)

        return importe
            .setScale(2)
            .toPlainString()
    }

    /**
     * Limpia mensajes.
     */
    fun limpiarMensaje() {

        _uiState.update {
            it.copy(
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Muestra un error.
     */
    private fun mostrarError(
        mensaje: String
    ) {

        _uiState.update {
            it.copy(
                error = mensaje,
                mensaje = null
            )
        }
    }
}