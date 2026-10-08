
package com.tapiceria.app.ui.pagos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tapiceria.app.data.local.entity.PagoEntity
import com.tapiceria.app.domain.repository.PagoRepository
import com.tapiceria.app.domain.repository.TrabajoRepository
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Administra el historial y registro de pagos.
 */
class PagoViewModel(
    private val trabajoRepository: TrabajoRepository,
    private val pagoRepository: PagoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PagoUiState())
    val uiState: StateFlow<PagoUiState> = _uiState.asStateFlow()

    private var trabajoSeleccionadoJob: Job? = null

    init {
        cargarTrabajos()
    }

    /**
     * Carga los trabajos disponibles para registrar pagos.
     */
    private fun cargarTrabajos() {
        viewModelScope.launch {
            trabajoRepository.observarTodos().collectLatest { trabajos ->
                _uiState.update { estado ->
                    estado.copy(
                        trabajos = trabajos,
                        cargando = false
                    )
                }

                // Conserva la selección si el trabajo sigue existiendo.
                val seleccionado = _uiState.value.trabajoSeleccionadoId
                val existe = trabajos.any { it.id == seleccionado }

                if (seleccionado == null || !existe) {
                    seleccionarTrabajo(trabajos.firstOrNull()?.id)
                }
            }
        }
    }

    /**
     * Selecciona un trabajo y observa sus pagos en tiempo real.
     */
    fun seleccionarTrabajo(trabajoId: Long?) {
        trabajoSeleccionadoJob?.cancel()

        if (trabajoId == null) {
            _uiState.update {
                it.copy(
                    trabajoSeleccionadoId = null,
                    pagos = emptyList(),
                    importeTrabajoCentavos = 0L,
                    totalPagadoCentavos = 0L
                )
            }
            return
        }

        val trabajo = _uiState.value.trabajos
            .firstOrNull { it.id == trabajoId }

        _uiState.update {
            it.copy(
                trabajoSeleccionadoId = trabajoId,
                importeTrabajoCentavos = trabajo?.importeCentavos ?: 0L,
                pagos = emptyList(),
                totalPagadoCentavos = 0L,
                error = null,
                mensaje = null
            )
        }

        trabajoSeleccionadoJob = viewModelScope.launch {
            // Room actualiza el historial y el total cuando cambian los pagos.
            launch {
                pagoRepository.observarPorTrabajo(trabajoId)
                    .collectLatest { pagos ->
                        _uiState.update { it.copy(pagos = pagos) }
                    }
            }

            pagoRepository.observarTotalPagado(trabajoId)
                .collectLatest { total ->
                    _uiState.update {
                        it.copy(totalPagadoCentavos = total)
                    }
                }
        }
    }

    fun cambiarImporte(valor: String) {
        // Acepta números y hasta dos decimales.
        if (valor.isEmpty() || valor.matches(Regex("^\\d{0,9}(\\.\\d{0,2})?$"))) {
            _uiState.update { it.copy(importe = valor, error = null) }
        }
    }

    fun cambiarMetodo(metodo: String) {
        _uiState.update { it.copy(metodo = metodo) }
    }

    fun cambiarReferencia(valor: String) {
        _uiState.update { it.copy(referencia = valor) }
    }

    fun cambiarNotas(valor: String) {
        _uiState.update { it.copy(notas = valor) }
    }

    /**
     * Valida y registra un pago.
     */
    fun registrarPago() {
        val estado = _uiState.value
        val trabajoId = estado.trabajoSeleccionadoId

        if (trabajoId == null) {
            mostrarError("Selecciona un trabajo.")
            return
        }

        val importeCentavos = convertirACentavos(estado.importe)

        if (importeCentavos == null || importeCentavos <= 0L) {
            mostrarError("Captura un importe válido mayor que cero.")
            return
        }

        if (importeCentavos > estado.saldoPendienteCentavos) {
            mostrarError("El pago no puede superar el saldo pendiente.")
            return
        }

        if (estado.guardando) return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }

            try {
                // Revalida el saldo con el valor más reciente de la base de datos.
                val totalActual = pagoRepository.obtenerTotalPagado(trabajoId)
                val trabajoActual = trabajoRepository.obtenerPorId(trabajoId)
                    ?: throw IllegalStateException(
                        "El trabajo ya no existe."
                    )

                // Un trabajo cancelado conserva sus pagos históricos,
                // pero no acepta nuevos pagos.
                if (trabajoActual.estado == "CANCELADO") {
                    mostrarError(
                        "No se pueden registrar pagos en un trabajo cancelado."
                    )
                    return@launch
                }

                val saldoActual =
                    (trabajoActual.importeCentavos - totalActual)
                        .coerceAtLeast(0L)

                if (importeCentavos > saldoActual) {
                    mostrarError("El saldo cambió. Actualiza el importe del pago.")
                    return@launch
                }

                val pago = PagoEntity(
                    trabajoId = trabajoId,
                    importeCentavos = importeCentavos,
                    metodo = estado.metodo,
                    referencia = estado.referencia.trim(),
                    notas = estado.notas.trim()
                )

                pagoRepository.insertar(pago)

                _uiState.update {
                    it.copy(
                        importe = "",
                        referencia = "",
                        notas = "",
                        mensaje = "Pago registrado correctamente."
                    )
                }
            } catch (ex: Exception) {
                mostrarError(
                    ex.message ?: "No fue posible registrar el pago."
                )
            } finally {
                _uiState.update { it.copy(guardando = false) }
            }
        }
    }

    /**
     * Elimina un pago únicamente si el trabajo asociado
     * no se encuentra en estado TERMINADO.
     *
     * Se consulta nuevamente la base de datos para evitar
     * eliminar un pago si el estado cambió desde la pantalla.
     */
    fun eliminarPago(pagoId: Long) {

        viewModelScope.launch {
            try {

                val trabajoId =
                    _uiState.value.trabajoSeleccionadoId
                        ?: run {
                            mostrarError("Selecciona un trabajo.")
                            return@launch
                        }

                // Consultamos el estado más reciente del trabajo.
                val trabajo = trabajoRepository.obtenerPorId(trabajoId)
                    ?: run {
                        mostrarError("El trabajo ya no existe.")
                        return@launch
                    }

                if (trabajo.estado == "TERMINADO" || trabajo.estado == "ENTREGADO") {
                    mostrarError(
                        "No se pueden eliminar pagos de un trabajo terminado."
                    )
                    return@launch
                }

                val resultado =
                    pagoRepository.eliminarPorId(pagoId)

                _uiState.update {
                    it.copy(
                        mensaje = if (resultado > 0) {
                            "Pago eliminado."
                        } else {
                            "No se encontró el pago."
                        },
                        error = null
                    )
                }

            } catch (ex: Exception) {
                mostrarError(
                    ex.message ?: "No fue posible eliminar el pago."
                )
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(error = null, mensaje = null) }
    }

    private fun mostrarError(mensaje: String) {
        _uiState.update { it.copy(error = mensaje, mensaje = null) }
    }

    /**
     * Convierte una cantidad decimal a centavos sin usar Float o Double.
     */
    private fun convertirACentavos(valor: String): Long? {
        return try {
            BigDecimal(valor)
                .setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
                .longValueExact()
        } catch (_: NumberFormatException) {
            null
        } catch (_: ArithmeticException) {
            null
        }
    }
}