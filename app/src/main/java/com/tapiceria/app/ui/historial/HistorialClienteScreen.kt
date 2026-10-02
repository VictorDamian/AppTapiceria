
package com.tapiceria.app.ui.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.tapiceria.app.data.local.entity.AtencionEntity
import com.tapiceria.app.data.local.entity.CotizacionEntity
import com.tapiceria.app.data.local.entity.FotoTrabajoEntity
import com.tapiceria.app.data.local.entity.PagoEntity
import com.tapiceria.app.data.local.entity.TrabajoEntity
import java.io.File
import java.text.NumberFormat
import java.util.Locale

/**
 * Pantalla de consulta del historial de un cliente.
 *
 * No modifica registros. Toda la información proviene del ViewModel.
 */
@Composable
fun HistorialClienteScreen(
    viewModel: HistorialClienteViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    val historial = estado.historial

    val formatoMoneda = NumberFormat.getCurrencyInstance(
        Locale("es", "MX")
    )

    // Convierte los centavos almacenados en la base de datos a moneda.
    fun moneda(centavos: Long): String {
        return formatoMoneda.format(centavos / 100.0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(onClick = onVolver) {
                Text("Volver")
            }
        }

        Text(
            text = "Historial del cliente",
            style = MaterialTheme.typography.headlineSmall
        )

        if (estado.cargando) {
            CircularProgressIndicator()
        } else if (estado.error != null) {
            Text(
                text = estado.error.orEmpty(),
                color = MaterialTheme.colorScheme.error
            )
        } else {
            val cliente = historial.cliente

            if (cliente != null) {
                // Datos generales y contacto.
                SeccionHistorial(titulo = "Datos del cliente") {
                    TextoDato("Nombre", cliente.nombre)
                    TextoDato("Teléfono", cliente.telefono)
                    TextoDato("Dirección", cliente.direccion)
                    TextoDato("Notas", cliente.notas)
                }

                // Resumen financiero.
                SeccionHistorial(titulo = "Resumen de pagos") {
                    TextoDato(
                        "Total abonado",
                        moneda(historial.totalPagadoCentavos)
                    )
                    TextoDato(
                        "Saldo pendiente estimado",
                        moneda(historial.saldoPendienteCentavos)
                    )
                }

                SeccionHistorial(
                    titulo = "Consultas (${historial.atenciones.count { it.tipo == "CONSULTA" }})"
                ) {
                    val consultas = historial.atenciones.filter {
                        it.tipo == "CONSULTA"
                    }

                    if (consultas.isEmpty()) {
                        TextoVacio("No hay consultas registradas.")
                    } else {
                        consultas.forEach { atencion ->
                            TarjetaAtencion(atencion)
                        }
                    }
                }

                SeccionHistorial(
                    titulo = "Cotizaciones (${historial.cotizaciones.size})"
                ) {
                    if (historial.cotizaciones.isEmpty()) {
                        TextoVacio("No hay cotizaciones registradas.")
                    } else {
                        historial.cotizaciones.forEach { cotizacion ->
                            TarjetaCotizacion(
                                cotizacion = cotizacion,
                                moneda = ::moneda
                            )
                        }
                    }
                }

                SeccionHistorial(
                    titulo = "Trabajos (${historial.trabajos.size})"
                ) {
                    if (historial.trabajos.isEmpty()) {
                        TextoVacio("No hay trabajos registrados.")
                    } else {
                        // Muestra cada trabajo junto con sus pagos y su saldo pendiente.
                        historial.trabajos.forEach { trabajo ->
                            TarjetaTrabajo(
                                trabajo = trabajo,
                                totalPagadoCentavos = historial.totalPagadoTrabajoCentavos(trabajo.id),
                                saldoPendienteCentavos = historial.saldoTrabajoCentavos(trabajo.id),
                                moneda = ::moneda
                            )
                        }
                    }
                }

                SeccionHistorial(
                    titulo = "Pagos (${historial.pagos.size})"
                ) {
                    if (historial.pagos.isEmpty()) {
                        TextoVacio("No hay pagos registrados.")
                    } else {
                        historial.pagos.forEach { pago ->
                            TarjetaPago(
                                pago = pago,
                                moneda = ::moneda
                            )
                        }
                    }
                }

                SeccionHistorial(
                    titulo = "Fotografías (${historial.fotografias.size})"
                ) {
                    if (historial.fotografias.isEmpty()) {
                        TextoVacio("No hay fotografías registradas.")
                    } else {
                        historial.fotografias.forEach { foto ->
                            TarjetaFotografia(foto)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionHistorial(
    titulo: String,
    contenido: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium
            )
            HorizontalDivider()
            contenido()
        }
    }
}

@Composable
private fun TextoDato(etiqueta: String, valor: String?) {
    if (!valor.isNullOrBlank()) {
        Text(
            text = "$etiqueta: $valor",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun TextoVacio(mensaje: String) {
    Text(
        text = mensaje,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun TarjetaAtencion(atencion: AtencionEntity) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(atencion.descripcion)
        TextoDato("Fecha", atencion.fechaAtencion.toString())
        TextoDato("Notas", atencion.notas)
        HorizontalDivider()
    }
}

@Composable
private fun TarjetaCotizacion(
    cotizacion: CotizacionEntity,
    moneda: (Long) -> String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = cotizacion.folio,
            style = MaterialTheme.typography.titleSmall
        )
        Text(cotizacion.descripcion)
        TextoDato("Importe", moneda(cotizacion.importeCentavos))
        TextoDato("Estado", cotizacion.estado)
        TextoDato("Vigencia", cotizacion.fechaVigencia.toString())
        HorizontalDivider()
    }
}

@Composable
private fun TarjetaTrabajo(
    trabajo: TrabajoEntity,
    totalPagadoCentavos: Long,
    saldoPendienteCentavos: Long,
    moneda: (Long) -> String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = trabajo.folio,
            style = MaterialTheme.typography.titleSmall
        )
        Text(trabajo.descripcion)
        TextoDato("Importe", moneda(trabajo.importeCentavos))
        TextoDato("Estado", trabajo.estado)
        TextoDato("Recepción", trabajo.fechaRecepcion.toString())
        TextoDato(
            "Entrega estimada",
            trabajo.fechaEntregaEstimada.toString()
        )

        // Resumen financiero del trabajo.
        TextoDato(
            etiqueta = "Total del trabajo",
            valor = moneda(trabajo.importeCentavos)
        )

        TextoDato(
            etiqueta = "Total pagado",
            valor = moneda(totalPagadoCentavos)
        )

        TextoDato(
            etiqueta = "Saldo pendiente",
            valor = moneda(saldoPendienteCentavos)
        )
        HorizontalDivider()
    }
}

@Composable
private fun TarjetaPago(
    pago: PagoEntity,
    moneda: (Long) -> String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = moneda(pago.importeCentavos),
            style = MaterialTheme.typography.titleSmall
        )
        TextoDato("Fecha", pago.fechaPago.toString())
        TextoDato("Método", pago.metodo)
        TextoDato("Referencia", pago.referencia)
        TextoDato("Notas", pago.notas)
        HorizontalDivider()
    }
}

@Composable
private fun TarjetaFotografia(foto: FotoTrabajoEntity) {
    val context = LocalContext.current

    // La base de datos almacena una ruta relativa, no la imagen completa.
    val archivo = File(context.filesDir, foto.rutaArchivo)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Tipo: ${foto.tipo}",
            style = MaterialTheme.typography.titleSmall
        )

        AsyncImage(
            model = archivo,
            contentDescription = "Fotografía ${foto.tipo}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentScale = ContentScale.FillWidth
        )

        TextoDato("Descripción", foto.descripcion)
        TextoDato("Fecha", foto.fechaRegistro.toString())
        HorizontalDivider()
    }
}