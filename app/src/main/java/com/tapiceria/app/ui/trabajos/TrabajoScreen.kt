
package com.tapiceria.app.ui.trabajos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tapiceria.app.data.local.entity.ClienteEntity
import com.tapiceria.app.domain.model.CotizacionTrabajoOpcion
import com.tapiceria.app.domain.model.TrabajoListado
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrabajoScreen(viewModel: TrabajoViewModel) {
    val estado by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Trabajos",
            style = MaterialTheme.typography.headlineMedium
        )

        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        estado.mensaje?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        FormularioTrabajo(
            clientes = estado.clientes,
            cotizaciones = estado.cotizaciones,
            clienteSeleccionadoId = estado.clienteSeleccionadoId,
            cotizacionSeleccionadaId = estado.cotizacionSeleccionadaId,
            descripcion = estado.descripcion,
            importe = estado.importe,
            fechaEntrega = estado.fechaEntregaEstimada,
            notas = estado.notas,
            guardando = estado.guardando,
            onSeleccionarCliente = viewModel::seleccionarCliente,
            onSeleccionarCotizacion = viewModel::seleccionarCotizacion,
            onDescripcionChange = viewModel::cambiarDescripcion,
            onImporteChange = viewModel::cambiarImporte,
            onFechaEntregaChange = viewModel::cambiarFechaEntrega,
            onNotasChange = viewModel::cambiarNotas,
            onGuardar = viewModel::guardarTrabajo
        )

        Text(
            text = "Seguimiento de trabajos",
            style = MaterialTheme.typography.titleLarge
        )

        when {
            estado.cargando -> CircularProgressIndicator()

            estado.trabajos.isEmpty() -> {
                Text("Todavía no hay trabajos registrados.")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(440.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = estado.trabajos,
                        key = { it.id }
                    ) { trabajo ->
                        TrabajoItem(
                            trabajo = trabajo,
                            onCambiarEstado = { nuevoEstado ->
                                viewModel.cambiarEstado(
                                    trabajo.id,
                                    nuevoEstado
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormularioTrabajo(
    clientes: List<ClienteEntity>,
    cotizaciones: List<CotizacionTrabajoOpcion>,
    clienteSeleccionadoId: Long?,
    cotizacionSeleccionadaId: Long?,
    descripcion: String,
    importe: String,
    fechaEntrega: String,
    notas: String,
    guardando: Boolean,
    onSeleccionarCliente: (Long) -> Unit,
    onSeleccionarCotizacion: (Long?) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onImporteChange: (String) -> Unit,
    onFechaEntregaChange: (String) -> Unit,
    onNotasChange: (String) -> Unit,
    onGuardar: () -> Unit
) {
    var menuClientes by remember { mutableStateOf(false) }
    var menuCotizaciones by remember { mutableStateOf(false) }

    val cliente = clientes.firstOrNull {
        it.id == clienteSeleccionadoId
    }

    // Solo permite seleccionar cotizaciones del cliente elegido.
    val cotizacionesCliente = cotizaciones.filter {
        it.clienteId == clienteSeleccionadoId
    }

    val cotizacion = cotizacionesCliente.firstOrNull {
        it.id == cotizacionSeleccionadaId
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Registrar trabajo",
                style = MaterialTheme.typography.titleMedium
            )

            Box {
                OutlinedButton(
                    onClick = { menuClientes = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = clientes.isNotEmpty() && !guardando
                ) {
                    Text(cliente?.nombre ?: "Seleccionar cliente")
                }

                DropdownMenu(
                    expanded = menuClientes,
                    onDismissRequest = { menuClientes = false }
                ) {
                    clientes.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.nombre) },
                            onClick = {
                                onSeleccionarCliente(item.id)
                                menuClientes = false
                            }
                        )
                    }
                }
            }

            Box {
                OutlinedButton(
                    onClick = { menuCotizaciones = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !guardando
                ) {
                    Text(
                        cotizacion?.let { "${it.folio} - ${it.descripcion}" }
                            ?: "Sin cotización / seleccionar"
                    )
                }

                DropdownMenu(
                    expanded = menuCotizaciones,
                    onDismissRequest = { menuCotizaciones = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sin cotización") },
                        onClick = {
                            onSeleccionarCotizacion(null)
                            menuCotizaciones = false
                        }
                    )

                    cotizacionesCliente.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(item.folio)
                                    Text(
                                        text = item.descripcion,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            onClick = {
                                onSeleccionarCotizacion(item.id)
                                menuCotizaciones = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = descripcion,
                onValueChange = onDescripcionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descripción del trabajo") },
                placeholder = {
                    Text("Ej. Retapizado de sala de tres piezas")
                },
                minLines = 2,
                maxLines = 4,
                enabled = !guardando
            )

            OutlinedTextField(
                value = importe,
                onValueChange = onImporteChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        if (cotizacion != null) {
                            "Importe de la cotización (MXN)"
                        } else {
                            "Importe acordado (MXN)"
                        }
                    )
                },
                placeholder = { Text("Ej. 2500.00") },
                enabled = !guardando && cotizacion == null,
                singleLine = true
            )

            if (cotizacion != null) {
                Text(
                    text = "El importe se tomará de la cotización aceptada.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = fechaEntrega,
                onValueChange = onFechaEntregaChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Entrega estimada (opcional)") },
                placeholder = { Text("AAAA-MM-DD") },
                singleLine = true,
                enabled = !guardando
            )

            OutlinedTextField(
                value = notas,
                onValueChange = onNotasChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notas (opcional)") },
                minLines = 2,
                maxLines = 4,
                enabled = !guardando
            )

            Button(
                onClick = onGuardar,
                modifier = Modifier.fillMaxWidth(),
                enabled = !guardando && clientes.isNotEmpty()
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(20.dp)
                            .height(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Guardando...")
                } else {
                    Text("Registrar trabajo")
                }
            }
        }
    }
}

@Composable
private fun TrabajoItem(
    trabajo: TrabajoListado,
    onCambiarEstado: (String) -> Unit
) {
    val importe = remember(trabajo.importeCentavos) {
        String.format(
            Locale("es", "MX"),
            "$%,.2f",
            trabajo.importeCentavos / 100.0
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = trabajo.folio,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = trabajo.nombreCliente,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(trabajo.descripcion)
            Text("Importe: $importe MXN")
            Text("Recepción: ${formatearFecha(trabajo.fechaRecepcion)}")

            trabajo.fechaEntregaEstimada?.let {
                Text("Entrega estimada: ${formatearFecha(it)}")
            }

            trabajo.fechaEntregaReal?.let {
                Text("Entregado: ${formatearFecha(it)}")
            }

            Text(
                text = "Estado: ${trabajo.estado}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge
            )

            // Los trabajos cerrados no permiten más cambios.
            when (trabajo.estado) {
                "PENDIENTE" -> {
                    Button(
                        onClick = { onCambiarEstado("EN_PROCESO") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Iniciar trabajo")
                    }
                }

                "EN_PROCESO" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onCambiarEstado("TERMINADO") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Terminado")
                        }

                        OutlinedButton(
                            onClick = { onCambiarEstado("CANCELADO") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                    }
                }

                "TERMINADO" -> {
                    Button(
                        onClick = { onCambiarEstado("ENTREGADO") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Registrar entrega")
                    }
                }
            }
        }
    }
}

private fun formatearFecha(fecha: Long): String {
    return SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    ).format(Date(fecha))
}