package com.tapiceria.app.ui.trabajos

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tapiceria.app.data.local.entity.ClienteEntity
import com.tapiceria.app.domain.model.CotizacionTrabajoOpcion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.tapiceria.app.domain.model.*

@Composable
fun TrabajoScreen(viewModel: TrabajoViewModel) {
    val estado by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ============================================================
        // ENCABEZADO
        // ============================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {


            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "Trabajos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Registra y da seguimiento a tus trabajos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ============================================================
        // MENSAJE DE ERROR
        // ============================================================

        estado.error?.let { mensaje ->

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // ============================================================
        // MENSAJE DE ÉXITO
        // ============================================================

        estado.mensaje?.let { mensaje ->

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // ============================================================
        // FORMULARIO
        // ============================================================

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

        // ============================================================
        // ENCABEZADO DE SEGUIMIENTO
        // ============================================================

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Seguimiento de trabajos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Consulta el estado y las fechas de cada trabajo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ============================================================
        // LISTADO
        // ============================================================

        when {

            estado.cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            estado.trabajos.isEmpty() -> {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Todavía no hay trabajos registrados.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(440.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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

    // Cliente actualmente seleccionado.
    val cliente = clientes.firstOrNull {
        it.id == clienteSeleccionadoId
    }

    // Solo mostramos las cotizaciones correspondientes
    // al cliente actualmente seleccionado.
    val cotizacionesCliente = cotizaciones.filter {
        it.clienteId == clienteSeleccionadoId
    }

    // Cotización actualmente seleccionada.
    val cotizacion = cotizacionesCliente.firstOrNull {
        it.id == cotizacionSeleccionadaId
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
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // --------------------------------------------------------
            // ENCABEZADO DEL FORMULARIO
            // --------------------------------------------------------

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Registrar trabajo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Captura los datos del nuevo trabajo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // --------------------------------------------------------
            // CLIENTE
            // --------------------------------------------------------

            Text(
                text = "Cliente",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box {
                OutlinedButton(
                    onClick = { menuClientes = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = clientes.isNotEmpty() && !guardando,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = cliente?.nombre ?: "Seleccionar cliente"
                    )
                }

                DropdownMenu(
                    expanded = menuClientes,
                    onDismissRequest = {
                        menuClientes = false
                    }
                ) {
                    clientes.forEach { item ->

                        DropdownMenuItem(
                            text = {
                                Text(item.nombre)
                            },
                            onClick = {
                                onSeleccionarCliente(item.id)
                                menuClientes = false
                            }
                        )
                    }
                }
            }

            // --------------------------------------------------------
            // COTIZACIÓN
            // --------------------------------------------------------

            Text(
                text = "Cotización",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box {
                OutlinedButton(
                    onClick = { menuCotizaciones = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !guardando,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = cotizacion?.let {
                            "${it.folio} - ${it.descripcion}"
                        } ?: "Sin cotización / seleccionar"
                    )
                }

                DropdownMenu(
                    expanded = menuCotizaciones,
                    onDismissRequest = {
                        menuCotizaciones = false
                    }
                ) {

                    DropdownMenuItem(
                        text = {
                            Text("Sin cotización")
                        },
                        onClick = {
                            onSeleccionarCotizacion(null)
                            menuCotizaciones = false
                        }
                    )

                    cotizacionesCliente.forEach { item ->

                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = item.folio,
                                        fontWeight = FontWeight.SemiBold
                                    )

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

            // --------------------------------------------------------
            // DESCRIPCIÓN
            // --------------------------------------------------------

            OutlinedTextField(
                value = descripcion,
                onValueChange = onDescripcionChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Descripción del trabajo")
                },
                placeholder = {
                    Text("Ej. Retapizado de sala de tres piezas")
                },
                minLines = 2,
                maxLines = 4,
                enabled = !guardando,
                shape = RoundedCornerShape(12.dp)
            )

            // --------------------------------------------------------
            // IMPORTE
            // --------------------------------------------------------

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
                placeholder = {
                    Text("Ej. 2500.00")
                },
                enabled = !guardando && cotizacion == null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Cuando existe una cotización seleccionada,
            // mostramos la explicación sin modificar la lógica.
            if (cotizacion != null) {

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "El importe se tomará de la cotización aceptada.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // --------------------------------------------------------
            // FECHA DE ENTREGA
            // --------------------------------------------------------

            OutlinedTextField(
                value = fechaEntrega,
                onValueChange = onFechaEntregaChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Entrega estimada (opcional)")
                },
                placeholder = {
                    Text("AAAA-MM-DD")
                },
                singleLine = true,
                enabled = !guardando,
                shape = RoundedCornerShape(12.dp)
            )

            // --------------------------------------------------------
            // NOTAS
            // --------------------------------------------------------

            OutlinedTextField(
                value = notas,
                onValueChange = onNotasChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Notas (opcional)")
                },
                minLines = 2,
                maxLines = 4,
                enabled = !guardando,
                shape = RoundedCornerShape(12.dp)
            )

            // --------------------------------------------------------
            // BOTÓN GUARDAR
            // --------------------------------------------------------

            Button(
                onClick = onGuardar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !guardando && clientes.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {

                if (guardando) {

                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(20.dp)
                            .height(20.dp),
                        strokeWidth = 2.dp
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text("Guardando...")

                } else {

                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

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
    // Formateamos el importe únicamente para presentación.
    // La información original y su tipo permanecen sin cambios.
    val importe = remember(trabajo.importeCentavos) {
        String.format(
            Locale("es", "MX"),
            "$%,.2f",
            trabajo.importeCentavos / 100.0
        )
    }

    // Color visual asociado al estado.
    // No modifica el estado real del trabajo.
    val colorEstado = when (trabajo.estado) {
        "PENDIENTE" -> MaterialTheme.colorScheme.secondary
        "EN_PROCESO" -> MaterialTheme.colorScheme.primary
        "TERMINADO" -> MaterialTheme.colorScheme.tertiary
        "ENTREGADO" -> MaterialTheme.colorScheme.primary
        "CANCELADO" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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

            // --------------------------------------------------------
            // FOLIO + ESTADO
            // --------------------------------------------------------

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = trabajo.folio,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = trabajo.nombreCliente,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colorEstado.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = trabajo.estado,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        color = colorEstado,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider()

            // --------------------------------------------------------
            // DESCRIPCIÓN
            // --------------------------------------------------------

            Text(
                text = trabajo.descripcion,
                style = MaterialTheme.typography.bodyLarge
            )

            // --------------------------------------------------------
            // IMPORTE
            // --------------------------------------------------------

            Text(
                text = "Importe: $importe MXN",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            // --------------------------------------------------------
            // FECHAS
            // --------------------------------------------------------

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    modifier = Modifier.width(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Recepción: ${formatearFecha(trabajo.fechaRecepcion)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            trabajo.fechaEntregaEstimada?.let {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.width(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Entrega estimada: ${formatearFecha(it)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            trabajo.fechaEntregaReal?.let {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.width(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Entregado: ${formatearFecha(it)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // --------------------------------------------------------
            // ACCIONES
            // --------------------------------------------------------

            when (trabajo.estado) {

                "PENDIENTE" -> {

                    Button(
                        onClick = {
                            onCambiarEstado("EN_PROCESO")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Iniciar trabajo")
                    }
                }

                "EN_PROCESO" -> {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Button(
                            onClick = {
                                onCambiarEstado("TERMINADO")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text("Terminado")
                        }

                        OutlinedButton(
                            onClick = {
                                onCambiarEstado("CANCELADO")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Cancelar")
                        }
                    }
                }

                "TERMINADO" -> {

                    Button(
                        onClick = {
                            onCambiarEstado("ENTREGADO")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Registrar entrega")
                    }
                }
            }
        }
    }
}

/**
 * Convierte una fecha almacenada como timestamp a un formato
 * amigable para mostrar al usuario.
 */
private fun formatearFecha(fecha: Long): String {
    return SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    ).format(Date(fecha))
}