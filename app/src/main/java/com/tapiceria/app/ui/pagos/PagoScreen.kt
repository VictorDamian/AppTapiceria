
package com.tapiceria.app.ui.pagos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tapiceria.app.data.local.entity.PagoEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla para registrar pagos y consultar saldos por trabajo.
 */
@Composable
fun PagoScreen(viewModel: PagoViewModel) {
    val estado by viewModel.uiState.collectAsState()

    var selectorTrabajosAbierto by remember { mutableStateOf(false) }
    var pagoPorEliminar by remember { mutableStateOf<PagoEntity?>(null) }

    val trabajoSeleccionado = estado.trabajos.firstOrNull {
        it.id == estado.trabajoSeleccionadoId
    }

    // Un trabajo cancelado conserva su historial,
    // pero no permite capturar nuevos pagos.
    val trabajoCancelado =
        trabajoSeleccionado?.estado == "CANCELADO"

    // Un trabajo terminado conserva sus pagos, pero no permite eliminarlos.
    val trabajoTerminado =
        trabajoSeleccionado?.estado == "TERMINADO"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Conserva aquí todos los elementos actuales de PagoScreen:
        // selector de trabajo, resumen, formulario e historial.
        Text(
            text = "Pagos y saldos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Column {
            OutlinedButton(
                onClick = { selectorTrabajosAbierto = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = estado.trabajos.isNotEmpty()
            ) {
                Text(
                    trabajoSeleccionado?.let {
                        "${it.folio} - ${it.nombreCliente}"
                    } ?: "Seleccionar trabajo"
                )
            }

            DropdownMenu(
                expanded = selectorTrabajosAbierto,
                onDismissRequest = {
                    selectorTrabajosAbierto = false
                }
            ) {
                estado.trabajos.forEach { trabajo ->
                    DropdownMenuItem(
                        text = {
                            Text("${trabajo.folio} - ${trabajo.nombreCliente}")
                        },
                        onClick = {
                            viewModel.seleccionarTrabajo(trabajo.id)
                            selectorTrabajosAbierto = false
                        }
                    )
                }
            }
        }

        if (estado.trabajos.isEmpty() && !estado.cargando) {
            Text("Primero registra un trabajo para poder capturar pagos.")
        }

        // Resumen del importe acordado, pagos y saldo pendiente.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilaImporte(
                    etiqueta = "Importe del trabajo",
                    centavos = estado.importeTrabajoCentavos
                )
                FilaImporte(
                    etiqueta = "Total pagado",
                    centavos = estado.totalPagadoCentavos
                )
                FilaImporte(
                    etiqueta = "Saldo pendiente",
                    centavos = estado.saldoPendienteCentavos,
                    destacado = true
                )
            }
        }

        if (trabajoCancelado) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Este trabajo está cancelado. " +
                            "Los pagos históricos se conservan, " +
                            "pero no se pueden registrar nuevos pagos.",
                    modifier = Modifier.padding(12.dp),
                    color =
                        MaterialTheme.colorScheme.onErrorContainer,
                    style =
                        MaterialTheme.typography.bodyMedium
                )
            }
        }

        Text(
            text = "Registrar pago",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = estado.importe,
            onValueChange = viewModel::cambiarImporte,
            label = { Text("Importe (ej. 1500.00)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            singleLine = true,
            enabled = trabajoSeleccionado != null &&
                    !trabajoCancelado &&
                    !estado.guardando
        )

        // Selector del método de pago.
        Text("Método de pago",
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("EFECTIVO", "TRANSFERENCIA").forEach { metodo ->
                OutlinedButton(
                    onClick = { viewModel.cambiarMetodo(metodo) },
                    modifier = Modifier.weight(1f),
                    enabled = !estado.guardando
                ) {
                    Text(
                        text = metodo,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Text(
            text = "Método seleccionado: ${estado.metodo}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = estado.referencia,
            onValueChange = viewModel::cambiarReferencia,
            label = { Text("Referencia (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !trabajoCancelado && !estado.guardando
        )

        OutlinedTextField(
            value = estado.notas,
            onValueChange = viewModel::cambiarNotas,
            label = { Text("Notas (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !trabajoCancelado && !estado.guardando,
            minLines = 2
        )

        Button(
            onClick = viewModel::registrarPago,
            modifier = Modifier.fillMaxWidth(),
            enabled = trabajoSeleccionado != null &&
                    !estado.guardando &&
                    !trabajoCancelado &&
                    estado.saldoPendienteCentavos > 0L
        ) {
            Text(if (estado.guardando) "Guardando..." else "Registrar pago")
        }

        estado.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        estado.mensaje?.let { mensaje ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = viewModel::limpiarMensajes) {
                    Text("Cerrar")
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (trabajoTerminado) {
            Text(
                text = "Este trabajo está terminado. " +
                        "Sus pagos se conservan y no pueden eliminarse.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = "Historial de pagos",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (trabajoSeleccionado != null && estado.pagos.isEmpty()) {
            Text("Este trabajo todavía no tiene pagos registrados.")
        }

        // El historial ocupa el espacio restante y permite desplazamiento.
        // El historial forma parte del desplazamiento de la pantalla.
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            estado.pagos.forEach { pago ->
                PagoItem(
                    pago = pago,
                    puedeEliminar = !trabajoTerminado,
                    onEliminar = {
                        if (!trabajoTerminado) {
                            pagoPorEliminar = pago
                        }
                    }
                )
            }
        }
    }

    // Se pide confirmación antes de eliminar un registro financiero.
    pagoPorEliminar?.let { pago ->
        AlertDialog(
            onDismissRequest = { pagoPorEliminar = null },
            title = { Text("Eliminar pago") },
            text = {
                Text(
                    "¿Deseas eliminar este pago de " +
                            formatoMoneda(pago.importeCentavos) +
                            "? El saldo pendiente aumentará."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarPago(pago.id)
                        pagoPorEliminar = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { pagoPorEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
/**
 * Presenta una fila del resumen financiero.
 * El saldo pendiente recibe mayor énfasis visual.
 */
@Composable
private fun FilaImporte(
    etiqueta: String,
    centavos: Long,
    destacado: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            modifier = Modifier.weight(1f),
            style = if (destacado) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = formatoMoneda(centavos),
            style = if (destacado) {
                MaterialTheme.typography.titleLarge
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = if (destacado) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

/**
 * Tarjeta individual de un pago registrado.
 */
@Composable
private fun PagoItem(
    pago: PagoEntity,
    puedeEliminar: Boolean,
    onEliminar: () -> Unit
) {
    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),

            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatoMoneda(pago.importeCentavos),
                    style = MaterialTheme.typography.titleMedium
                )

                Text("Método: ${pago.metodo}")
                Text(
                    "Fecha: ${formatoFecha.format(Date(pago.fechaPago))}",
                    style = MaterialTheme.typography.bodySmall
                )

                if (pago.referencia.isNotBlank()) {
                    Text("Referencia: ${pago.referencia}")
                }

                if (pago.notas.isNotBlank()) {
                    Text("Notas: ${pago.notas}")
                }
            }

            TextButton(
                onClick = onEliminar,
                enabled = puedeEliminar
            ) {
                Text(
                    text = if (puedeEliminar) {
                        "Eliminar"
                    } else {
                        "No disponible"
                    }
                )
            }
        }
    }
}

/**
 * Formatea centavos como moneda mexicana.
 */
private fun formatoMoneda(centavos: Long): String {
    val importe = centavos / 100.0
    return NumberFormat.getCurrencyInstance(
        Locale("es", "MX")
    ).format(importe)
}