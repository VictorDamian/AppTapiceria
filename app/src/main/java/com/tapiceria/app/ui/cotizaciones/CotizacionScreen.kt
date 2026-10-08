package com.tapiceria.app.ui.cotizaciones

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tapiceria.app.domain.model.AtencionListado
import com.tapiceria.app.domain.model.CotizacionListado
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

/**
 * Pantalla de administración de cotizaciones.
 */
@Composable
fun CotizacionScreen(
    viewModel: CotizacionViewModel
) {

    val estado by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
            .padding(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {

        item {
            Text(
                text = "Da de alta una cotización",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {

            estado.error?.let { mensaje ->

                Text(
                    text = mensaje,
                    color =
                        MaterialTheme.colorScheme.error
                )
            }

            estado.mensaje?.let { mensaje ->

                Text(
                    text = mensaje,
                    color =
                        MaterialTheme.colorScheme.primary
                )
            }
        }

        item {

            FormularioCotizacion(
                atenciones = estado.atenciones,
                textoBusqueda =
                    estado.textoBusquedaAtencion,
                atencionSeleccionadaId =
                    estado.atencionSeleccionadaId,
                descripcion =
                    estado.descripcion,
                importe =
                    estado.importe,
                fechaVigencia =
                    estado.fechaVigencia,
                cotizacionEditandoId =
                    estado.cotizacionEditandoId,
                guardando =
                    estado.guardando,
                onBusquedaChange =
                    viewModel::cambiarBusquedaAtencion,
                onSeleccionarAtencion =
                    viewModel::seleccionarAtencion,
                onDescripcionChange =
                    viewModel::cambiarDescripcion,
                onImporteChange =
                    viewModel::cambiarImporte,
                onFechaVigenciaChange =
                    viewModel::cambiarFechaVigencia,
                onGuardar =
                    viewModel::guardarCotizacion,
                onCancelarEdicion =
                    viewModel::cancelarEdicion,
            )
        }

        item {

            Text(
                text = "Historial",
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (estado.cargando) {

            item {
                CircularProgressIndicator()
            }

        } else if (estado.cotizaciones.isEmpty()) {

            item {

                Text(
                    text =
                        "Todavía no hay cotizaciones registradas.",
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }

        } else {

            items(
                items = estado.cotizaciones,
                key = { it.id }
            ) { cotizacion ->

                CotizacionItem(
                    cotizacion = cotizacion,
                    onEditar = {
                        viewModel.editarCotizacion(
                            cotizacion.id
                        )
                    },
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
                    },
                    onPendiente = {
                        viewModel.cambiarEstado(
                            cotizacion.id,
                            "PENDIENTE"
                        )
                    }
                )
            }
        }
    }
}

/**
 * Formulario de creación y edición.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioCotizacion(
    atenciones: List<AtencionListado>,
    textoBusqueda: String,
    atencionSeleccionadaId: Long?,
    descripcion: String,
    importe: String,
    fechaVigencia: String,
    cotizacionEditandoId: Long?,
    guardando: Boolean,
    onBusquedaChange: (String) -> Unit,
    onSeleccionarAtencion: (Long) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onImporteChange: (String) -> Unit,
    onFechaVigenciaChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelarEdicion: () -> Unit
) {

    var mostrarCalendario by remember {
        mutableStateOf(false)
    }

    var autocompleteExpandido by remember {
        mutableStateOf(false)
    }

    /**
     * Una atención sin cliente no puede generar cotización,
     * por lo tanto no se muestra como opción.
     */
    val atencionesDisponibles =
        atenciones.filter {
            it.clienteId != null
        }

    val atencionesFiltradas =
        atencionesDisponibles
            .filter { atencion ->

                val texto =
                    textoBusqueda.trim()

                texto.isBlank() ||
                        atencion.nombreCliente.contains(
                            texto,
                            ignoreCase = true
                        ) ||
                        atencion.descripcion.contains(
                            texto,
                            ignoreCase = true
                        )
            }
            .take(10)

    val atencionSeleccionada =
        atenciones.firstOrNull {
            it.id == atencionSeleccionadaId
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text =
                    if (cotizacionEditandoId == null) {
                        "Nueva cotización"
                    } else {
                        "Editar cotización"
                    },
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            /**
             * Buscador de solicitudes.
             */
            /**
             * Autocomplete para buscar solicitudes.
             *
             * Solo se muestran los primeros resultados coincidentes
             * para evitar listas enormes en pantalla.
             */
            ExposedDropdownMenuBox(
                expanded = autocompleteExpandido,
                onExpandedChange = {
                    if (!guardando) {
                        autocompleteExpandido = !autocompleteExpandido
                    }
                }
            ) {

                OutlinedTextField(
                    value = textoBusqueda,

                    onValueChange = { texto ->

                        onBusquedaChange(texto)

                        /*
                         * Al escribir mostramos inmediatamente
                         * los resultados disponibles.
                         */
                        autocompleteExpandido = true
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),

                    label = {
                        Text("Buscar solicitud")
                    },

                    placeholder = {
                        Text("Cliente o descripción")
                    },

                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = autocompleteExpandido
                        )
                    },

                    singleLine = true,

                    enabled = !guardando
                )

                ExposedDropdownMenu(
                    expanded = autocompleteExpandido,
                    onDismissRequest = {
                        autocompleteExpandido = false
                    }
                ) {

                    if (atencionesFiltradas.isEmpty()) {

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "No se encontraron solicitudes."
                                )
                            },
                            onClick = {
                                autocompleteExpandido = false
                            }
                        )

                    } else {

                        atencionesFiltradas.forEach { atencion ->

                            DropdownMenuItem(
                                text = {

                                    Column {

                                        Text(
                                            text =
                                                atencion.nombreCliente,
                                            fontWeight =
                                                FontWeight.Medium
                                        )

                                        Text(
                                            text =
                                                atencion.descripcion,
                                            style =
                                                MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },

                                onClick = {

                                    onSeleccionarAtencion(
                                        atencion.id
                                    )

                                    /*
                                     * Después de seleccionar limpiamos
                                     * el texto de búsqueda.
                                     */
                                    onBusquedaChange("")

                                    autocompleteExpandido = false
                                }
                            )
                        }
                    }
                }
            }

            /**
             * Atención seleccionada.
             */
            if (atencionSeleccionada != null) {

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color =
                        MaterialTheme.colorScheme
                            .primaryContainer
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(
                            text =
                                "Solicitud seleccionada",
                            style =
                                MaterialTheme.typography
                                    .labelMedium
                        )

                        Text(
                            text =
                                atencionSeleccionada
                                    .nombreCliente,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                atencionSeleccionada
                                    .descripcion,
                            style =
                                MaterialTheme.typography
                                    .bodySmall
                        )
                    }
                }
            }


            if (
                atencionesDisponibles.isEmpty()
            ) {

                Text(
                    text =
                        "No hay solicitudes de cotización " +
                                "con cliente asignado.",
                    color =
                        MaterialTheme.colorScheme.error,
                    style =
                        MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = descripcion,
                onValueChange =
                    onDescripcionChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Concepto de la cotización")
                },
                placeholder = {
                    Text(
                        "Ej. Retapizado con tela tipo lino"
                    )
                },
                minLines = 2,
                maxLines = 4,
                enabled = !guardando
            )

            OutlinedTextField(
                value = importe,
                onValueChange =
                    onImporteChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Importe (MXN)")
                },
                placeholder = {
                    Text("Ej. 1250.50")
                },
                singleLine = true,
                enabled = !guardando
            )

            /**
             * Selector de fecha.
             *
             * Ya no obligamos al usuario a escribir
             * manualmente AAAA-MM-DD.
             */
            OutlinedButton(
                onClick = {
                    mostrarCalendario = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !guardando
            ) {

                Text(
                    text =
                        if (fechaVigencia.isBlank()) {
                            "Seleccionar fecha de vigencia"
                        } else {
                            "Vigencia: $fechaVigencia"
                        }
                )
            }

            if (fechaVigencia.isNotBlank()) {

                TextButton(
                    onClick = {
                        onFechaVigenciaChange("")
                    },
                    enabled = !guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Quitar fecha de vigencia")
                }
            }

            Button(
                onClick = onGuardar,
                modifier = Modifier.fillMaxWidth(),
                enabled =
                    !guardando &&
                            atencionSeleccionadaId != null
            ) {

                if (guardando) {

                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(20.dp)
                            .height(20.dp),
                        strokeWidth = 2.dp
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text("Guardando...")

                } else {

                    Text(
                        if (
                            cotizacionEditandoId == null
                        ) {
                            "Registrar cotización"
                        } else {
                            "Guardar cambios"
                        }
                    )
                }
            }

            if (
                cotizacionEditandoId != null
            ) {

                TextButton(
                    onClick =
                        onCancelarEdicion,
                    modifier =
                        Modifier.fillMaxWidth(),
                    enabled = !guardando
                ) {
                    Text("Cancelar edición")
                }
            }
        }
    }

    /**
     * Diálogo de selección de fecha.
     */
    if (mostrarCalendario) {

        val fechaInicial =
            convertirTextoAFecha(fechaVigencia)

        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    fechaInicial
            )

        DatePickerDialog(
            onDismissRequest = {
                mostrarCalendario = false
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        val millis =
                            datePickerState
                                .selectedDateMillis

                        if (millis != null) {

                            onFechaVigenciaChange(
                                convertirFechaATexto(
                                    millis
                                )
                            )
                        }

                        mostrarCalendario = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        mostrarCalendario = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }
}

/**
 * Tarjeta de una cotización.
 */
@Composable
private fun CotizacionItem(
    cotizacion: CotizacionListado,
    onEditar: () -> Unit,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    onPendiente: () -> Unit
) {

    val importe =
        cotizacion.importeCentavos / 100.0

    val importeFormateado =
        String.format(
            Locale("es", "MX"),
            "$%,.2f",
            importe
        )

    val colorEstado =
        when (cotizacion.estado) {

            "ACEPTADA" ->
                MaterialTheme.colorScheme.primary

            "RECHAZADA",
            "VENCIDA" ->
                MaterialTheme.colorScheme.error

            else ->
                MaterialTheme.colorScheme.secondary
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text = cotizacion.folio,
                    style =
                        MaterialTheme.typography
                            .titleMedium,
                    color =
                        MaterialTheme.colorScheme
                            .primary,
                    fontWeight = FontWeight.Bold
                )

                /*
 * Las cotizaciones PENDIENTE, ACEPTADA y RECHAZADA
 * pueden editarse.
 *
 * El ViewModel realizará la validación adicional
 * para saber si la cotización está ligada a un Trabajo activo.
 */
                if (
                    cotizacion.estado == "PENDIENTE" ||
                    cotizacion.estado == "ACEPTADA" ||
                    cotizacion.estado == "RECHAZADA"
                ) {

                    TextButton(
                        onClick = onEditar
                    ) {
                        Text("Editar")
                    }
                }
            }

            Text(
                text = cotizacion.nombreCliente,
                style =
                    MaterialTheme.typography.titleLarge
            )

            Text(
                text =
                    cotizacion.descripcionCotizacion,
                style =
                    MaterialTheme.typography.bodyLarge
            )

            Text(
                text =
                    "Solicitud: " +
                            cotizacion.descripcionAtencion,
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color =
                    MaterialTheme.colorScheme
                        .secondaryContainer
            ) {

                Column(
                    modifier = Modifier.padding(12.dp)
                ) {

                    Text(
                        text = "Importe",
                        style =
                            MaterialTheme.typography
                                .labelMedium
                    )

                    Text(
                        text =
                            "$importeFormateado MXN",
                        style =
                            MaterialTheme.typography
                                .titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text =
                    "Creada: " +
                            formatearFecha(
                                cotizacion.fechaCreacion
                            ),
                style =
                    MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            cotizacion.fechaVigencia?.let { fecha ->

                Text(
                    text =
                        "Vigencia: " +
                                formatearFecha(fecha),
                    style =
                        MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color =
                    colorEstado.copy(
                        alpha = 0.12f
                    )
            ) {

                Text(
                    text = cotizacion.estado,
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 6.dp
                    ),
                    color = colorEstado,
                    style =
                        MaterialTheme.typography
                            .labelLarge
                )
            }

            /**
             * Las acciones de aceptar/rechazar solamente
             * aparecen cuando sigue pendiente.
             */
            HorizontalDivider()

            when (cotizacion.estado) {

                "PENDIENTE" -> {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
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

                "ACEPTADA" -> {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        OutlinedButton(
                            onClick = onRechazar,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Rechazar")
                        }

                        OutlinedButton(
                            onClick = onPendiente,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Pendiente")
                        }
                    }
                }

                "RECHAZADA" -> {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        Button(
                            onClick = onAceptar,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Aceptar")
                        }
                        OutlinedButton(
                            onClick = onPendiente,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Pendiente")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Convierte un texto yyyy-MM-dd a milisegundos.
 *
 * Se utiliza UTC porque DatePicker trabaja la fecha seleccionada
 * como medianoche UTC.
 */
private fun convertirTextoAFecha(
    valor: String
): Long? {

    if (valor.isBlank()) {
        return null
    }

    return try {

        SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.ROOT
        ).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse(valor)?.time

    } catch (_: Exception) {

        null
    }
}

/**
 * Convierte milisegundos del DatePicker a yyyy-MM-dd.
 *
 * Se utiliza UTC para evitar que la zona horaria local
 * convierta, por ejemplo, el día 10 en el día 9.
 */
private fun convertirFechaATexto(
    fecha: Long
): String {

    return SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.ROOT
    ).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(
        Date(fecha)
    )
}

/**
 * Formatea una fecha para mostrarla al usuario.
 */
private fun formatearFecha(
    fecha: Long
): String {

    return SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    ).format(Date(fecha))
}