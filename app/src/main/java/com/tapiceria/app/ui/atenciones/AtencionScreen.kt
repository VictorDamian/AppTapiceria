
package com.tapiceria.app.ui.atenciones

import androidx.compose.foundation.background
import androidx.compose.material3.Surface
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tapiceria.app.data.local.entity.ClienteEntity
import com.tapiceria.app.domain.model.AtencionListado
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla para registrar y consultar atenciones de clientes.
 */
@Composable
fun AtencionScreen(
    viewModel: AtencionViewModel
) {
    val estado by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Conserva el contenido actual de la pantalla.
        Text(
            text = "Atenciones",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Registra consultas y solicitudes de cotización.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Muestra mensajes de validación o de resultado.
        estado.error?.let { mensaje ->
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }

        estado.mensaje?.let { mensaje ->
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.primary
            )
        }

        FormularioAtencion(
            clientes = estado.clientes,
            clienteSeleccionadoId = estado.clienteSeleccionadoId,
            tipoSeleccionado = estado.tipoSeleccionado,
            descripcion = estado.descripcion,
            notas = estado.notas,
            guardando = estado.guardando,
            onSeleccionarCliente = viewModel::seleccionarCliente,
            onSeleccionarTipo = viewModel::seleccionarTipo,
            onDescripcionChange = viewModel::cambiarDescripcion,
            onNotasChange = viewModel::cambiarNotas,
            onGuardar = viewModel::guardarAtencion
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Historial de atenciones",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground

        )

        when {
            estado.cargando -> {
                CircularProgressIndicator()
            }

            estado.atenciones.isEmpty() -> {
                Text(
                    text = "Todavía no hay atenciones registradas.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> {
                // La lista muestra las atenciones más recientes primero.
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = estado.atenciones,
                        key = { atencion -> atencion.id }
                    ) { atencion ->
                        AtencionItem(atencion = atencion)
                    }
                }
            }
        }
    }
}

/**
 * Formulario para capturar los datos de una atención.
 */
@Composable
private fun FormularioAtencion(
    clientes: List<ClienteEntity>,
    clienteSeleccionadoId: Long?,
    tipoSeleccionado: String,
    descripcion: String,
    notas: String,
    guardando: Boolean,
    onSeleccionarCliente: (Long) -> Unit,
    onSeleccionarTipo: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onNotasChange: (String) -> Unit,
    onGuardar: () -> Unit
) {
    var menuClientesAbierto by remember { mutableStateOf(false) }

    val clienteSeleccionado = clientes.firstOrNull {
        it.id == clienteSeleccionadoId
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        // Conserva el Column y todos los campos actuales del formulario.
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Nueva atención",
                style = MaterialTheme.typography.titleMedium
            )

            // Selector de cliente activo.
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { menuClientesAbierto = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = clientes.isNotEmpty() && !guardando
                ) {
                    Text(
                        text = clienteSeleccionado?.nombre
                            ?: "Seleccionar cliente"
                    )
                }

                DropdownMenu(
                    expanded = menuClientesAbierto,
                    onDismissRequest = {
                        menuClientesAbierto = false
                    },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    clientes.forEach { cliente ->
                        DropdownMenuItem(
                            text = { Text(cliente.nombre) },
                            onClick = {
                                onSeleccionarCliente(cliente.id)
                                menuClientesAbierto = false
                            }
                        )
                    }
                }
            }

            if (clientes.isEmpty()) {
                Text(
                    text = "Primero registra un cliente activo.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "Tipo de atención",
                style = MaterialTheme.typography.labelLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onSeleccionarTipo("CONSULTA") },
                    enabled = !guardando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (tipoSeleccionado == "CONSULTA") {
                            "✓ Consulta"
                        } else {
                            "Consulta"
                        }
                    )
                }

                OutlinedButton(
                    onClick = { onSeleccionarTipo("COTIZACION") },
                    enabled = !guardando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (tipoSeleccionado == "COTIZACION") {
                            "✓ Cotización"
                        } else {
                            "Cotización"
                        }
                    )
                }
            }

            OutlinedTextField(
                value = descripcion,
                onValueChange = onDescripcionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descripción") },
                placeholder = {
                    Text("Ej. Reparación de sillón de tres plazas")
                },
                minLines = 2,
                maxLines = 4,
                enabled = !guardando
            )

            OutlinedTextField(
                value = notas,
                onValueChange = onNotasChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notas (opcional)") },
                placeholder = {
                    Text("Detalles adicionales del cliente")
                },
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardando...")
                } else {
                    Text("Registrar atención")
                }
            }
        }
    }
}

/**
 * Tarjeta de una atención registrada.
 */
@Composable
private fun AtencionItem(
    atencion: AtencionListado
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = atencion.nombreCliente,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // El tipo se muestra como una etiqueta visual.
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = if (atencion.tipo == "COTIZACION") {
                        "Solicitud de cotización"
                    } else {
                        "Consulta"
                    },
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Text(
                text = atencion.descripcion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (atencion.notas.isNotBlank()) {
                Text(
                    text = "Notas: ${atencion.notas}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = "Fecha: ${formatearFecha(atencion.fechaAtencion)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Convierte la fecha almacenada en milisegundos a un formato legible.
 */
private fun formatearFecha(fecha: Long): String {
    val formato = SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale.getDefault()
    )

    return formato.format(Date(fecha))
}