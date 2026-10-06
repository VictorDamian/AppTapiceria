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
 * Gestiona el registro, edición y seguimiento de los trabajos.
 */
class TrabajoViewModel(
    private val trabajoRepository: TrabajoRepository,
    private val clienteRepository: ClienteRepository,
    private val pagoRepository: PagoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrabajoUiState())
    val uiState: StateFlow<TrabajoUiState> = _uiState.asStateFlow()

    /**
     * Estados válidos del flujo de trabajo.
     */
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

    /**
     * Observa todos los trabajos.
     */
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
                        it.copy(
                            trabajos = trabajos,
                            cargando = false
                        )
                    }
                }
        }
    }

    /**
     * Observa únicamente clientes activos.
     */
    private fun observarClientes() {
        viewModelScope.launch {
            clienteRepository.observarActivos()
                .catch {
                    mostrarError("No fue posible cargar los clientes.")
                }
                .collect { clientes ->

                    _uiState.update { estado ->

                        val clienteActual = estado.clienteSeleccionadoId

                        val existe = clientes.any {
                            it.id == clienteActual
                        }

                        estado.copy(
                            clientes = clientes,
                            clienteSeleccionadoId = if (existe) {
                                clienteActual
                            } else {
                                clientes.firstOrNull()?.id
                            }
                        )
                    }
                }
        }
    }

    /**
     * Observa cotizaciones aceptadas.
     */
    private fun observarCotizaciones() {
        viewModelScope.launch {
            trabajoRepository.observarCotizacionesAceptadas()
                .catch {
                    mostrarError(
                        "No fue posible cargar las cotizaciones."
                    )
                }
                .collect { cotizaciones ->

                    _uiState.update { estado ->

                        val cotizacionActual =
                            estado.cotizacionSeleccionadaId

                        val existe = cotizaciones.any {
                            it.id == cotizacionActual &&
                                    it.clienteId ==
                                    estado.clienteSeleccionadoId
                        }

                        estado.copy(
                            cotizaciones = cotizaciones,
                            cotizacionSeleccionadaId =
                                if (existe) {
                                    cotizacionActual
                                } else {
                                    null
                                }
                        )
                    }
                }
        }
    }

    /**
     * Cambia el texto de búsqueda de clientes.
     */
    fun cambiarBusquedaCliente(valor: String) {
        _uiState.update {
            it.copy(
                textoBusquedaCliente = valor,
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Cambia el texto de búsqueda de cotizaciones.
     */
    fun cambiarBusquedaCotizacion(valor: String) {
        _uiState.update {
            it.copy(
                textoBusquedaCotizacion = valor,
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Selecciona un cliente.
     *
     * Al cambiar el cliente se limpia la cotización seleccionada
     * porque una cotización pertenece a un cliente específico.
     */
    fun seleccionarCliente(id: Long) {

        _uiState.update {
            it.copy(
                clienteSeleccionadoId = id,
                cotizacionSeleccionadaId = null,
                textoBusquedaCotizacion = "",
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Selecciona una cotización.
     */
    fun seleccionarCotizacion(id: Long?) {

        val estado = _uiState.value

        val cotizacion = estado.cotizaciones.firstOrNull {
            it.id == id &&
                    it.clienteId ==
                    estado.clienteSeleccionadoId
        }

        _uiState.update {

            it.copy(
                cotizacionSeleccionadaId =
                    cotizacion?.id,

                descripcion =
                    cotizacion?.descripcion
                        ?: it.descripcion,

                importe =
                    cotizacion?.let {
                        BigDecimal.valueOf(
                            it.importeCentavos,
                            2
                        )
                            .setScale(2)
                            .toPlainString()
                    } ?: it.importe,

                textoBusquedaCotizacion = "",
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Modifica la descripción.
     */
    fun cambiarDescripcion(valor: String) {

        _uiState.update {
            it.copy(
                descripcion = valor,
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Modifica el importe.
     *
     * Solamente permite hasta dos decimales.
     */
    fun cambiarImporte(valor: String) {

        val normalizado = valor.replace(',', '.')

        if (
            normalizado.isEmpty() ||
            normalizado.matches(
                Regex("^\\d{0,9}(\\.\\d{0,2})?$")
            )
        ) {

            _uiState.update {
                it.copy(
                    importe = normalizado,
                    error = null,
                    mensaje = null
                )
            }
        }
    }

    /**
     * Modifica la fecha de entrega estimada.
     */
    fun cambiarFechaEntrega(valor: String) {

        _uiState.update {
            it.copy(
                fechaEntregaEstimada = valor,
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Modifica las notas.
     */
    fun cambiarNotas(valor: String) {

        _uiState.update {
            it.copy(
                notas = valor,
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Carga un trabajo existente en el formulario.
     */
    fun editarTrabajo(id: Long) {

        viewModelScope.launch {

            try {

                val trabajo =
                    trabajoRepository.obtenerPorId(id)

                if (trabajo == null) {
                    mostrarError(
                        "No se encontró el trabajo."
                    )
                    return@launch
                }

                // Un trabajo entregado ya no se puede editar.
                if (trabajo.estado == "ENTREGADO") {
                    mostrarError(
                        "Un trabajo entregado ya no puede editarse."
                    )
                    return@launch
                }

                val clienteExiste =
                    _uiState.value.clientes.any {
                        it.id == trabajo.clienteId
                    }

                if (!clienteExiste) {
                    mostrarError(
                        "El cliente del trabajo ya no está activo."
                    )
                    return@launch
                }

                val importe =
                    BigDecimal.valueOf(
                        trabajo.importeCentavos,
                        2
                    )
                        .setScale(2)
                        .toPlainString()

                val fecha =
                    trabajo.fechaEntregaEstimada?.let {
                        SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.ROOT
                        ).format(it)
                    } ?: ""

                _uiState.update {

                    it.copy(
                        trabajoEditandoId = trabajo.id,
                        clienteSeleccionadoId =
                            trabajo.clienteId,
                        cotizacionSeleccionadaId =
                            trabajo.cotizacionId,
                        descripcion =
                            trabajo.descripcion,
                        importe = importe,
                        fechaEntregaEstimada = fecha,
                        notas = trabajo.notas,
                        textoBusquedaCliente = "",
                        textoBusquedaCotizacion = "",
                        error = null,
                        mensaje =
                            "Editando ${trabajo.folio}"
                    )
                }

            } catch (_: Exception) {

                mostrarError(
                    "No fue posible cargar el trabajo."
                )
            }
        }
    }

    /**
     * Cancela la edición y deja nuevamente el formulario
     * listo para registrar un trabajo.
     */
    fun cancelarEdicion() {

        _uiState.update {

            it.copy(
                trabajoEditandoId = null,
                clienteSeleccionadoId =
                    it.clientes.firstOrNull()?.id,
                cotizacionSeleccionadaId = null,
                descripcion = "",
                importe = "",
                fechaEntregaEstimada = "",
                notas = "",
                textoBusquedaCliente = "",
                textoBusquedaCotizacion = "",
                error = null,
                mensaje = null
            )
        }
    }

    /**
     * Guarda un trabajo nuevo o actualiza uno existente.
     */
    fun guardarTrabajo() {

        val estado = _uiState.value

        if (estado.guardando) return

        val clienteId =
            estado.clienteSeleccionadoId

        if (
            clienteId == null ||
            estado.clientes.none {
                it.id == clienteId
            }
        ) {
            mostrarError(
                "Selecciona un cliente activo."
            )
            return
        }

        val cotizacion =
            estado.cotizaciones.firstOrNull {

                it.id ==
                        estado.cotizacionSeleccionadaId &&
                        it.clienteId ==
                        clienteId
            }

        if (
            estado.cotizacionSeleccionadaId != null &&
            cotizacion == null
        ) {
            mostrarError(
                "La cotización seleccionada ya no está disponible."
            )
            return
        }

        val descripcion =
            estado.descripcion.trim()

        if (descripcion.isBlank()) {
            mostrarError(
                "La descripción del trabajo es obligatoria."
            )
            return
        }

        /**
         * Si existe una cotización, su importe es el importe
         * inicial del trabajo.
         *
         * Si no existe, se toma el importe capturado manualmente.
         */
        val importeCentavos =
            if (cotizacion != null) {

                cotizacion.importeCentavos

            } else {

                convertirImporteCentavos(
                    estado.importe
                ) ?: run {

                    mostrarError(
                        "Ingresa un importe válido mayor que cero."
                    )

                    return
                }
            }

        if (importeCentavos <= 0L) {

            mostrarError(
                "El importe debe ser mayor que cero."
            )

            return
        }

        val fechaEntrega =
            if (estado.fechaEntregaEstimada.isBlank()) {

                null

            } else {

                convertirFechaFinDia(
                    estado.fechaEntregaEstimada
                ) ?: run {

                    mostrarError(
                        "Usa el formato de fecha AAAA-MM-DD."
                    )

                    return
                }
            }

        if (
            fechaEntrega != null &&
            fechaEntrega <
            inicioDelDia(System.currentTimeMillis())
        ) {

            mostrarError(
                "La fecha de entrega debe ser hoy o posterior."
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

                val idEditando =
                    estado.trabajoEditandoId

                if (idEditando == null) {

                    // ------------------------------------------------
                    // NUEVO TRABAJO
                    // ------------------------------------------------

                    val trabajo =
                        TrabajoEntity(
                            clienteId = clienteId,
                            cotizacionId =
                                cotizacion?.id,
                            folio =
                                "TRAB-${System.currentTimeMillis()}",
                            descripcion = descripcion,
                            importeCentavos =
                                importeCentavos,
                            fechaEntregaEstimada =
                                fechaEntrega,
                            notas =
                                estado.notas.trim()
                        )

                    trabajoRepository.insertar(
                        trabajo
                    )

                    limpiarFormulario()

                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mensaje =
                                "Trabajo registrado: ${trabajo.folio}"
                        )
                    }

                } else {

                    // ------------------------------------------------
                    // EDICIÓN
                    // ------------------------------------------------

                    val existente =
                        trabajoRepository.obtenerPorId(
                            idEditando
                        )

                    if (existente == null) {

                        mostrarError(
                            "El trabajo ya no existe."
                        )

                        _uiState.update {
                            it.copy(
                                guardando = false
                            )
                        }

                        return@launch
                    }

                    if (existente.estado == "ENTREGADO") {

                        mostrarError(
                            "Un trabajo entregado no puede modificarse."
                        )

                        _uiState.update {
                            it.copy(
                                guardando = false
                            )
                        }

                        return@launch
                    }

                    /**
                     * Regla financiera importante:
                     *
                     * El nuevo importe jamás puede ser menor
                     * que el total de pagos realizados.
                     */
                    val totalPagado =
                        pagoRepository
                            .obtenerTotalPagado(
                                existente.id
                            )

                    if (
                        importeCentavos <
                        totalPagado
                    ) {

                        mostrarError(
                            "El importe no puede ser menor que " +
                                    "el total pagado (${
                                        formatearMoneda(
                                            totalPagado
                                        )
                                    })."
                        )

                        _uiState.update {
                            it.copy(
                                guardando = false
                            )
                        }

                        return@launch
                    }

                    val actualizado =
                        existente.copy(

                            // El folio original se conserva.
                            folio = existente.folio,

                            clienteId =
                                clienteId,

                            cotizacionId =
                                cotizacion?.id,

                            descripcion =
                                descripcion,

                            importeCentavos =
                                importeCentavos,

                            fechaEntregaEstimada =
                                fechaEntrega,

                            notas =
                                estado.notas.trim()
                        )

                    trabajoRepository.actualizar(
                        actualizado
                    )

                    val folio =
                        existente.folio

                    limpiarFormulario()

                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mensaje =
                                "Trabajo actualizado: $folio"
                        )
                    }
                }

            } catch (_: Exception) {

                _uiState.update {
                    it.copy(
                        guardando = false,
                        error =
                            "No fue posible guardar el trabajo."
                    )
                }
            }
        }
    }

    /**
     * Cambia el estado del trabajo.
     *
     * Reglas:
     * - ENTREGADO es definitivo.
     * - ENTREGADO solamente puede venir de TERMINADO.
     * - CANCELADO puede volver a un estado operativo.
     * - Cancelar nunca elimina pagos.
     */
    fun cambiarEstado(
        id: Long,
        nuevoEstado: String
    ) {

        if (nuevoEstado !in estadosPermitidos) {
            mostrarError(
                "Estado no válido."
            )
            return
        }

        viewModelScope.launch {

            try {

                val trabajo =
                    trabajoRepository.obtenerPorId(id)

                if (trabajo == null) {

                    mostrarError(
                        "No se encontró el trabajo."
                    )

                    return@launch
                }

                /**
                 * ENTREGADO es el único estado completamente final.
                 */
                if (trabajo.estado == "ENTREGADO") {

                    mostrarError(
                        "Un trabajo entregado ya no puede cambiar de estado."
                    )

                    return@launch
                }

                /**
                 * No permitimos marcar como entregado
                 * desde ningún estado diferente de TERMINADO.
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

                /**
                 * Si se cancela, únicamente cambia el estado.
                 *
                 * Los pagos permanecen intactos.
                 */
                val fechaEntregaReal =
                    if (nuevoEstado == "ENTREGADO") {

                        System.currentTimeMillis()

                    } else if (
                        nuevoEstado == "CANCELADO"
                    ) {

                        // Al cancelar no se elimina historial.
                        trabajo.fechaEntregaReal

                    } else {

                        // Si se recupera de CANCELADO,
                        // conservamos la fecha existente solamente
                        // si realmente fue una entrega anterior.
                        trabajo.fechaEntregaReal
                    }

                val actualizado =
                    trabajo.copy(
                        estado = nuevoEstado,
                        fechaEntregaReal =
                            fechaEntregaReal
                    )

                trabajoRepository.actualizar(
                    actualizado
                )

                _uiState.update {
                    it.copy(
                        mensaje =
                            when (nuevoEstado) {
                                "CANCELADO" ->
                                    "Trabajo cancelado. Los pagos históricos se conservaron."

                                else ->
                                    "Estado del trabajo actualizado."
                            }
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
     * Limpia el formulario.
     */
    private fun limpiarFormulario() {

        _uiState.update {

            it.copy(
                trabajoEditandoId = null,
                clienteSeleccionadoId =
                    it.clientes.firstOrNull()?.id,
                cotizacionSeleccionadaId = null,
                descripcion = "",
                importe = "",
                fechaEntregaEstimada = "",
                notas = "",
                textoBusquedaCliente = "",
                textoBusquedaCotizacion = ""
            )
        }
    }

    /**
     * Convierte un importe decimal a centavos.
     */
    private fun convertirImporteCentavos(
        valor: String
    ): Long? {

        return try {

            val decimal =
                valor.toBigDecimalOrNull()
                    ?: return null

            if (decimal <= BigDecimal.ZERO) {
                return null
            }

            decimal
                .setScale(
                    2,
                    RoundingMode.UNNECESSARY
                )
                .movePointRight(2)
                .longValueExact()

        } catch (_: ArithmeticException) {

            null
        }
    }

    /**
     * Convierte una fecha AAAA-MM-DD
     * al final del día.
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

        return Calendar.getInstance().apply {

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

        }.timeInMillis
    }

    /**
     * Obtiene el inicio del día actual.
     */
    private fun inicioDelDia(
        fecha: Long
    ): Long {

        return Calendar.getInstance().apply {

            timeInMillis = fecha

            set(
                Calendar.HOUR_OF_DAY,
                0
            )

            set(
                Calendar.MINUTE,
                0
            )

            set(
                Calendar.SECOND,
                0
            )

            set(
                Calendar.MILLISECOND,
                0
            )

        }.timeInMillis
    }

    /**
     * Formatea centavos como moneda mexicana.
     */
    private fun formatearMoneda(
        centavos: Long
    ): String {

        return String.format(
            Locale("es", "MX"),
            "$%,.2f",
            centavos / 100.0
        )
    }

    /**
     * Limpia los mensajes de pantalla.
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