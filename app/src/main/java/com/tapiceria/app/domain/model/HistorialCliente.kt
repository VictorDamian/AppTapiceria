package com.tapiceria.app.domain.model

import com.tapiceria.app.data.local.entity.*

/**
 * Agrupa toda la información relacionada con un cliente.
 */
data class HistorialCliente(
    val cliente: ClienteEntity? = null,
    val atenciones: List<AtencionEntity> = emptyList(),
    val cotizaciones: List<CotizacionEntity> = emptyList(),
    val trabajos: List<TrabajoEntity> = emptyList(),
    val pagos: List<PagoEntity> = emptyList(),
    val fotografias: List<FotoTrabajoEntity> = emptyList()
) {

    /**
     * Calcula cuánto se ha pagado en total por todos los trabajos.
     */
    val totalPagadoCentavos: Long
        get() = pagos.sumOf { it.importeCentavos }

    /**
     * Calcula el saldo pendiente sumando el saldo de cada trabajo.
     *
     * Se evita compensar el saldo de un trabajo con pagos excedentes
     * de otro trabajo.
     */
    val saldoPendienteCentavos: Long
        get() = trabajos.sumOf { trabajo ->
            saldoTrabajoCentavos(trabajo.id)
        }

    /**
     * Obtiene el importe pagado para un trabajo específico.
     */
    fun totalPagadoTrabajoCentavos(trabajoId: Long): Long {
        return pagos
            .filter { pago -> pago.trabajoId == trabajoId }
            .sumOf { pago -> pago.importeCentavos }
    }

    /**
     * Obtiene el saldo pendiente de un trabajo específico.
     *
     * El saldo mínimo mostrado es cero para evitar importes negativos
     * cuando los pagos superan el importe registrado del trabajo.
     */
    fun saldoTrabajoCentavos(trabajoId: Long): Long {
        val trabajo = trabajos.firstOrNull { it.id == trabajoId }
            ?: return 0L

        val totalPagado = totalPagadoTrabajoCentavos(trabajoId)

        return (trabajo.importeCentavos - totalPagado)
            .coerceAtLeast(0L)
    }
}