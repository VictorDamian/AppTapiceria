
package com.tapiceria.app.ui.cotizaciones

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
import androidx.compose.material3.CardDefaults
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
import com.tapiceria.app.domain.model.AtencionListado
import com.tapiceria.app.domain.model.CotizacionListado
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.ui.text.font.FontWeight

/**
 * Pantalla para registrar y administrar cotizaciones.
 */
@Composable
fun CotizacionScreen(
    viewModel: CotizacionViewModel
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
        // Conserva el contenido actual de CotizacionScreen.
        Text(
            text = "Cotizaciones",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        estado.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        estado.mensaje?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary
            )
        }

        FormularioCotizacion(
            atenciones = estado.atenciones,
            atencionSeleccionadaId = estado.atencionSeleccionadaId,
            descripcion = estado.descripcion,
            importe = estado.importe,
            fechaVigencia = estado.fechaVigencia,
            guardando = estado.guardando,
            onSeleccionarAtencion = viewModel::seleccionarAtencion,
            onDescripcionChange = viewModel::cambiarDescripcion,
            onImporteChange = viewModel::cambiarImporte,
            onFechaVigenciaChange = viewModel::cambiarFechaVigencia,
            onGuardar = viewModel::guardarCotizacion
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Historial",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        when {
            estado.cargando -> CircularProgressIndicator()

            estado.cotizaciones.isEmpty() -> {
                Text("Todavía no hay cotizaciones registradas.")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = estado.cotizaciones,
                        key = { it.id }
                    ) { cotizacion ->
                        CotizacionItem(
                            cotizacion = cotizacion,
                            onAceptar = {
                                viewModel.cambiarEstado(
                                    cotizacion.id,
                                    "ACEPTADA"
                                )
                            },
                            onRechazar = {
                                viewModel.cambiarEstado(
                                    cotizacion.id,
                                    "RECHAZADA"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formulario de alta de una cotización.
 */
@Composable
private fun FormularioCotizacion(
    atenciones: List<AtencionListado>,
    atencionSeleccionadaId: Long?,
    descripcion: String,
    importe: String,
    fechaVigencia: String,
    guardando: Boolean,
    onSeleccionarAtencion: (Long) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onImporteChange: (String) -> Unit,
    onFechaVigenciaChange: (String) -> Unit,
    onGuardar: () -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }

    val atencionSeleccionada = atenciones.firstOrNull {
        it.id == atencionSeleccionadaId
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
        // Conserva todos los campos y el botón de registro actuales.
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Nueva cotización",
                style = MaterialTheme.typography.titleMedium
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { menuAbierto = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = atenciones.isNotEmpty() && !guardando
                ) {
                    Text(
                        text = atencionSeleccionada?.let {
                            "${it.nombreCliente} - ${it.descripcion}"
                        } ?: "Seleccionar solicitud"
                    )
                }

                DropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = { menuAbierto = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    atenciones.forEach { atencion ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(atencion.nombreCliente)
                                    Text(
                                        text = atencion.descripcion,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            onClick = {
                                onSeleccionarAtencion(atencion.id)
                                menuAbierto = false
                            }
                        )
                    }
                }
            }

            if (atenciones.isEmpty()) {
                Text(
                    text = "Primero registra una atención de tipo Cotización.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = descripcion,
                onValueChange = onDescripcionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Concepto de la cotización") },
                placeholder = {
                    Text("Ej. Retapizado con tela tipo lino")
                },
                minLines = 2,
                maxLines = 4,
                enabled = !guardando
            )

            OutlinedTextField(
                value = importe,
                onValueChange = onImporteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Importe (MXN)") },
                placeholder = { Text("Ej. 1250.50") },
                singleLine = true,
                enabled = !guardando
            )

            OutlinedTextField(
                value = fechaVigencia,
                onValueChange = onFechaVigenciaChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Fecha de vigencia (opcional)") },
                placeholder = { Text("AAAA-MM-DD") },
                supportingText = {
                    Text("Ejemplo: 2026-12-31. La fecha incluye todo el día.")
                },
                singleLine = true,
                enabled = !guardando
            )

            Button(
                onClick = onGuardar,
                modifier = Modifier.fillMaxWidth(),
                enabled = !guardando && atenciones.isNotEmpty()
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
                    Text("Registrar cotización")
                }
            }
        }
    }
}

/**
 * Tarjeta de una cotización registrada.
 */
/**
 * Tarjeta visual de una cotización.
 * Conserva los importes, fechas, estados y acciones existentes.
 */
@Composable
private fun CotizacionItem(
    cotizacion: CotizacionListado,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    val importeFormateado = remember(cotizacion.importeCentavos) {
        val importe = cotizacion.importeCentavos / 100.0
        String.format(Locale("es", "MX"), "$%,.2f", importe)
    }

    val colorEstado = when (cotizacion.estado) {
        "ACEPTADA" -> MaterialTheme.colorScheme.primary
        "RECHAZADA", "VENCIDA" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }

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
            Text(
                text = cotizacion.folio,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = cotizacion.nombreCliente,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = cotizacion.descripcionCotizacion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Solicitud: ${cotizacion.descripcionAtencion}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Destacamos el importe sin modificar su cálculo.
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Importe",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Text(
                        text = "$importeFormateado MXN",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Text(
                text = "Creada: ${formatearFecha(cotizacion.fechaCreacion)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            cotizacion.fechaVigencia?.let { fecha ->
                Text(
                    text = "Vigencia: ${formatearFecha(fecha)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Etiqueta de estado con el color correspondiente.
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colorEstado.copy(alpha = 0.12f)
            ) {
                Text(
                    text = cotizacion.estado,
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 6.dp
                    ),
                    color = colorEstado,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Las acciones solo aparecen para cotizaciones pendientes.
            if (cotizacion.estado == "PENDIENTE") {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAceptar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aceptar")
                    }

                    OutlinedButton(
                        onClick = onRechazar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Rechazar")
                    }
                }
            }
        }
    }
}

/**
 * Formatea una fecha almacenada como milisegundos Unix.
 */
private fun formatearFecha(fecha: Long): String {
    return SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    ).format(Date(fecha))
}