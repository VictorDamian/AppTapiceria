
package com.tapiceria.app.data.repository

import com.tapiceria.app.data.local.dao.HistorialClienteDao
import com.tapiceria.app.data.local.entity.*
import com.tapiceria.app.domain.model.HistorialCliente
import com.tapiceria.app.domain.repository.HistorialClienteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Implementación del repositorio del historial.
 */
class HistorialClienteRepositoryImpl(
    private val historialDao: HistorialClienteDao
) : HistorialClienteRepository {

    override fun observarHistorial(clienteId: Long): Flow<HistorialCliente> {
        // 2. Combinamos los primeros 5 flujos utilizando el 'combine' estándar.
        // Aquí la inferencia de tipos funciona de forma nativa sin errores.
        val primerosCincoFlujos: Flow<ConjuntoHistorialTemporal> = combine(
            historialDao.observarCliente(clienteId),
            historialDao.observarAtenciones(clienteId),
            historialDao.observarCotizaciones(clienteId),
            historialDao.observarTrabajos(clienteId),
            historialDao.observarPagos(clienteId)
        ) { cliente, atenciones, cotizaciones, trabajos, pagos ->
            ConjuntoHistorialTemporal(
                cliente = cliente,
                atenciones = atenciones,
                cotizaciones = cotizaciones,
                trabajos = trabajos,
                pagos = pagos
            )
        }

        // 3. Tomamos el resultado anterior y lo combinamos con el sexto flujo restante.
        return primerosCincoFlujos.combine(
            historialDao.observarFotografias(clienteId)
        ) { datosTemporales, fotografias ->

            // 4. Construimos finalmente tu objeto de pantalla definitivo.
            HistorialCliente(
                cliente = datosTemporales.cliente,
                atenciones = datosTemporales.atenciones,
                cotizaciones = datosTemporales.cotizaciones,
                trabajos = datosTemporales.trabajos,
                pagos = datosTemporales.pagos,
                fotografias = fotografias
            )
        }
    }
}

// 1. Definimos una clase de datos auxiliar para agrupar los primeros 5 flujos.
// Nota: Asegúrate de que los nombres de los tipos (Cliente, Atencion, etc.)
// coincidan exactamente con las entidades o POJOs de tu proyecto.
private data class ConjuntoHistorialTemporal(
    val cliente: ClienteEntity?,
    val atenciones: List<AtencionEntity>,
    val cotizaciones: List<CotizacionEntity>,
    val trabajos: List<TrabajoEntity>,
    val pagos: List<PagoEntity>
)
