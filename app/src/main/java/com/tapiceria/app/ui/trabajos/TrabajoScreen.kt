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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.tapiceria.app.domain.model.TrabajoListado
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import java.util.TimeZone

/**
 * Pantalla principal de trabajos.
 *
 * Permite:
 * - Registrar trabajos.
 * - Buscar clientes.
 * - Buscar cotizaciones.
 * - Editar trabajos.
 * - Cambiar estados.
 * - Recuperar trabajos cancelados.
 */
@Composable
fun TrabajoScreen(
    viewModel: TrabajoViewModel
) {
    val estado by viewModel.uiState.collectAsState()

    /*
     * Filtramos los clientes únicamente para la presentación.
     * La fuente original permanece en el ViewModel.
     */
    val clientesFiltrados = remember(
        estado.clientes,
        estado.textoBusquedaCliente
    ) {
        val texto = estado.textoBusquedaCliente.trim()

        if (texto.isBlank()) {
            estado.clientes.take(10)
        } else {
            estado.clientes
                .filter {
                    it.nombre.contains(
                        texto,
                        ignoreCase = true
                    )
                }
                .take(10)
        }
    }

    /*
     * Filtramos las cotizaciones únicamente para presentación.
     */
    val cotizacionesCliente = remember(
        estado.cotizaciones,
        estado.clienteSeleccionadoId,
        estado.textoBusquedaCotizacion
    ) {
        val texto = estado.textoBusquedaCotizacion.trim()

        estado.cotizaciones
            .filter {
                it.clienteId ==
                        estado.clienteSeleccionadoId
            }
            .filter {
                texto.isBlank() ||
                        it.folio.contains(
                            texto,
                            ignoreCase = true
                        ) ||
                        it.descripcion.contains(
                            texto,
                            ignoreCase = true
                        )
            }
            .take(10)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
            .verticalScroll(
                rememberScrollState()
            )
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

            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

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
                    containerColor =
                        MaterialTheme.colorScheme.errorContainer
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

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    Text(
                        text = mensaje,
                        color =
                            MaterialTheme.colorScheme.onErrorContainer,
                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // ============================================================
        // MENSAJE DE ÉXITO / INFORMACIÓN
        // ============================================================

        estado.mensaje?.let { mensaje ->

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer
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

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    Text(
                        text = mensaje,
                        color =
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // ============================================================
        // FORMULARIO
        // ============================================================

        FormularioTrabajo(
            clientes = clientesFiltrados,
            cotizaciones = cotizacionesCliente,
            todosLosClientes = estado.clientes,
            clienteSeleccionadoId =
                estado.clienteSeleccionadoId,
            cotizacionSeleccionadaId =
                estado.cotizacionSeleccionadaId,
            textoBusquedaCliente =
                estado.textoBusquedaCliente,
            textoBusquedaCotizacion =
                estado.textoBusquedaCotizacion,
            trabajoEditandoId =
                estado.trabajoEditandoId,
            descripcion =
                estado.descripcion,
            importe =
                estado.importe,
            fechaEntrega =
                estado.fechaEntregaEstimada,
            notas =
                estado.notas,
            guardando =
                estado.guardando,
            onBusquedaClienteChange =
                viewModel::cambiarBusquedaCliente,
            onBusquedaCotizacionChange =
                viewModel::cambiarBusquedaCotizacion,
            onSeleccionarCliente =
                viewModel::seleccionarCliente,
            onSeleccionarCotizacion =
                viewModel::seleccionarCotizacion,
            onDescripcionChange =
                viewModel::cambiarDescripcion,
            onFechaEntregaChange =
                viewModel::cambiarFechaEntrega,
            onNotasChange =
                viewModel::cambiarNotas,
            onGuardar =
                viewModel::guardarTrabajo,
            onCancelarEdicion =
                viewModel::cancelarEdicion
        )

        // ============================================================
        // SEGUIMIENTO
        // ============================================================

        Column(
            verticalArrangement =
                Arrangement.spacedBy(2.dp)
        ) {

            Text(
                text = "Seguimiento de trabajos",
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    "Consulta el estado y las fechas de cada trabajo",
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
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
                    contentAlignment =
                        Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            estado.trabajos.isEmpty() -> {

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.surface
                        ),
                    shape =
                        RoundedCornerShape(16.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Build,
                            contentDescription = null,
                            tint =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )

                        Text(
                            text =
                                "Todavía no hay trabajos registrados.",
                            style =
                                MaterialTheme.typography.bodyLarge,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }
            }

            else -> {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    items(
                        items = estado.trabajos,
                        key = { it.id }
                    ) { trabajo ->

                        TrabajoItem(
                            trabajo = trabajo,

                            onEditar = {
                                viewModel.editarTrabajo(
                                    trabajo.id
                                )
                            },

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

/**
 * Formulario para crear o editar un trabajo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioTrabajo(
    clientes: List<ClienteEntity>,
    cotizaciones: List<CotizacionTrabajoOpcion>,
    todosLosClientes: List<ClienteEntity>,
    clienteSeleccionadoId: Long?,
    cotizacionSeleccionadaId: Long?,
    textoBusquedaCliente: String,
    textoBusquedaCotizacion: String,
    trabajoEditandoId: Long?,
    descripcion: String,
    importe: String,
    fechaEntrega: String,
    notas: String,
    guardando: Boolean,
    onBusquedaClienteChange: (String) -> Unit,
    onBusquedaCotizacionChange: (String) -> Unit,
    onSeleccionarCliente: (Long) -> Unit,
    onSeleccionarCotizacion: (Long?) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onFechaEntregaChange: (String) -> Unit,
    onNotasChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelarEdicion: () -> Unit
) {
    var mostrarCalendario by remember {
        mutableStateOf(false)
    }

    val clienteSeleccionado =
        todosLosClientes.firstOrNull {
            it.id == clienteSeleccionadoId
        }

    val cotizacionSeleccionada =
        cotizaciones.firstOrNull {
            it.id == cotizacionSeleccionadaId
        }

    var autocompleteClienteExpandido by remember {
        mutableStateOf(false)
    }

    var autocompleteCotizacionExpandido by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            // --------------------------------------------------------
            // ENCABEZADO
            // --------------------------------------------------------

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector =
                        if (trabajoEditandoId == null) {
                            Icons.Default.Build
                        } else {
                            Icons.Default.Edit
                        },
                    contentDescription = null,
                    tint =
                        MaterialTheme.colorScheme.primary
                )

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Column {

                    Text(
                        text =
                            if (trabajoEditandoId == null) {
                                "Registrar trabajo"
                            } else {
                                "Editar trabajo"
                            },
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text =
                            if (trabajoEditandoId == null) {
                                "Captura los datos del nuevo trabajo"
                            } else {
                                "Modifica los datos del trabajo existente"
                            },
                        style =
                            MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // --------------------------------------------------------
            // CLIENTE
            // --------------------------------------------------------

            Text(
                text = "Cliente",
                style =
                    MaterialTheme.typography.labelLarge,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            /**
             * Autocomplete de clientes.
             *
             * Permite escribir el nombre y seleccionar directamente
             * uno de los resultados encontrados.
             */
            ExposedDropdownMenuBox(
                expanded = autocompleteClienteExpandido,
                onExpandedChange = {
                    if (!guardando) {
                        autocompleteClienteExpandido =
                            !autocompleteClienteExpandido
                    }
                }
            ) {

                OutlinedTextField(
                    value = textoBusquedaCliente,

                    onValueChange = { texto ->

                        onBusquedaClienteChange(texto)

                        autocompleteClienteExpandido = true
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),

                    label = {
                        Text("Buscar cliente")
                    },

                    placeholder = {
                        Text("Nombre del cliente")
                    },

                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },

                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded =
                                autocompleteClienteExpandido
                        )
                    },

                    singleLine = true,

                    enabled = !guardando,

                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = autocompleteClienteExpandido,
                    onDismissRequest = {
                        autocompleteClienteExpandido = false
                    }
                ) {

                    if (clientes.isEmpty()) {

                        DropdownMenuItem(
                            text = {
                                Text("No se encontraron clientes.")
                            },
                            onClick = {
                                autocompleteClienteExpandido = false
                            }
                        )

                    } else {

                        clientes.forEach { cliente ->

                            DropdownMenuItem(
                                text = {

                                    Column {

                                        Text(
                                            text = cliente.nombre,
                                            fontWeight =
                                                FontWeight.Medium
                                        )

                                        if (cliente.telefono.isNotBlank()) {

                                            Text(
                                                text = cliente.telefono,
                                                style =
                                                    MaterialTheme.typography
                                                        .bodySmall
                                            )
                                        }
                                    }
                                },

                                onClick = {

                                    onSeleccionarCliente(
                                        cliente.id
                                    )

                                    onBusquedaClienteChange("")

                                    autocompleteClienteExpandido = false
                                }
                            )
                        }
                    }
                }
            }

            clienteSeleccionado?.let { cliente ->

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme
                                    .primaryContainer
                        ),
                    shape =
                        RoundedCornerShape(10.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Person,
                            contentDescription = null,
                            tint =
                                MaterialTheme.colorScheme
                                    .primary
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text = "Cliente seleccionado",
                                style =
                                    MaterialTheme.typography
                                        .labelSmall
                            )

                            Text(
                                text = cliente.nombre,
                                fontWeight =
                                    FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            /*
             * Resultados de búsqueda.
             *
             * Solamente se muestran cuando el usuario está
             * buscando un cliente.
             */
            if (
                textoBusquedaCliente.isNotBlank() &&
                clientes.isNotEmpty()
            ) {

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(10.dp)
                ) {

                    Column {

                        clientes.forEach { cliente ->

                            OutlinedButton(
                                onClick = {
                                    onSeleccionarCliente(
                                        cliente.id
                                    )
                                    onBusquedaClienteChange(
                                        ""
                                    )
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 4.dp,
                                            vertical = 2.dp
                                        ),
                                enabled = !guardando
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.Person,
                                    contentDescription = null
                                )

                                Spacer(
                                    modifier =
                                        Modifier.width(8.dp)
                                )

                                Text(
                                    text = cliente.nombre,
                                    modifier =
                                        Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            if (
                textoBusquedaCliente.isNotBlank() &&
                clientes.isEmpty()
            ) {

                Text(
                    text =
                        "No se encontraron clientes.",
                    style =
                        MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }

            // --------------------------------------------------------
            // COTIZACIÓN
            // --------------------------------------------------------

            Text(
                text = "Cotización",
                style =
                    MaterialTheme.typography.labelLarge,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            if (clienteSeleccionado == null) {

                Text(
                    text =
                        "Selecciona primero un cliente.",
                    style =
                        MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )

            } else {

                OutlinedTextField(
                    value =
                        textoBusquedaCotizacion,
                    onValueChange =
                        onBusquedaCotizacionChange,
                    modifier =
                        Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            "Buscar cotización"
                        )
                    },
                    placeholder = {
                        Text(
                            "Folio o descripción"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector =
                                Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    enabled = !guardando,
                    shape =
                        RoundedCornerShape(12.dp)
                )

                cotizacionSeleccionada?.let {

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme
                                        .secondaryContainer
                            ),
                        shape =
                            RoundedCornerShape(10.dp)
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(12.dp)
                        ) {

                            Text(
                                text =
                                    "Cotización seleccionada",
                                style =
                                    MaterialTheme.typography
                                        .labelSmall
                            )

                            Text(
                                text =
                                    it.folio,
                                fontWeight =
                                    FontWeight.Bold
                            )

                            Text(
                                text =
                                    it.descripcion,
                                style =
                                    MaterialTheme.typography
                                        .bodySmall
                            )

                            Text(
                                text =
                                    "Importe: ${
                                        formatoMoneda(
                                            it.importeCentavos
                                        )
                                    }",
                                style =
                                    MaterialTheme.typography
                                        .bodySmall
                            )

                            OutlinedButton(
                                onClick = {
                                    onSeleccionarCotizacion(
                                        null
                                    )
                                    onBusquedaCotizacionChange(
                                        ""
                                    )
                                },
                                enabled = !guardando
                            ) {
                                Text(
                                    "Quitar cotización"
                                )
                            }
                        }
                    }
                }

                if (
                    textoBusquedaCotizacion.isNotBlank() &&
                    cotizaciones.isNotEmpty()
                ) {

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),
                        shape =
                            RoundedCornerShape(10.dp)
                    ) {

                        Column {

                            cotizaciones.forEach { cotizacion ->

                                OutlinedButton(
                                    onClick = {

                                        onSeleccionarCotizacion(
                                            cotizacion.id
                                        )

                                        onBusquedaCotizacionChange(
                                            ""
                                        )
                                    },
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                horizontal = 4.dp,
                                                vertical = 2.dp
                                            ),
                                    enabled = !guardando
                                ) {

                                    Column(
                                        modifier =
                                            Modifier.weight(1f),
                                        horizontalAlignment =
                                            Alignment.Start
                                    ) {

                                        Text(
                                            text =
                                                cotizacion.folio,
                                            fontWeight =
                                                FontWeight.SemiBold
                                        )

                                        Text(
                                            text =
                                                cotizacion.descripcion,
                                            style =
                                                MaterialTheme.typography
                                                    .bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (
                    textoBusquedaCotizacion.isNotBlank() &&
                    cotizaciones.isEmpty()
                ) {

                    Text(
                        text =
                            "No se encontraron cotizaciones aceptadas.",
                        style =
                            MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            // --------------------------------------------------------
            // DESCRIPCIÓN
            // --------------------------------------------------------

            OutlinedTextField(
                value = descripcion,
                onValueChange =
                    onDescripcionChange,
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text(
                        "Descripción del trabajo"
                    )
                },
                placeholder = {
                    Text(
                        "Ej. Retapizado de sala de tres piezas"
                    )
                },
                minLines = 2,
                maxLines = 4,
                enabled = !guardando,
                shape =
                    RoundedCornerShape(12.dp)
            )

            // --------------------------------------------------------
            // IMPORTE
            // --------------------------------------------------------

            OutlinedTextField(
                value = importe,
                onValueChange = {
                    // El importe es solamente informativo.
                    // No se permite modificarlo desde Trabajos.
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text("Importe de la cotización (MXN)")
                },
                singleLine = true,

                readOnly = true,

                enabled = !guardando,

                shape = RoundedCornerShape(12.dp)
            )
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme
                                .secondaryContainer
                    ),
                shape =
                    RoundedCornerShape(10.dp)
            ) {

                Row(
                    modifier =
                        Modifier.padding(12.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint =
                            MaterialTheme.colorScheme
                                .secondary
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text(
                        text =
                            "El importe pertenece a la cotización. " +
                                    "Para modificarlo debes editar la cotización.",
                        style =
                            MaterialTheme.typography
                                .bodySmall
                    )
                }
            }
        }

        // --------------------------------------------------------
        // FECHA DE ENTREGA
        // --------------------------------------------------------

        Text(
            text = "Fecha de entrega",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(
            onClick = {
                mostrarCalendario = true
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando,
            shape = RoundedCornerShape(12.dp)
        ) {

            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text =
                    if (fechaEntrega.isBlank()) {
                        "Seleccionar fecha de entrega"
                    } else {
                        "Entrega: $fechaEntrega"
                    }
            )
        }

        if (fechaEntrega.isNotBlank()) {

            TextButton(
                onClick = {
                    onFechaEntregaChange("")
                },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Quitar fecha")
            }
        }

        /**
         * Diálogo de selección de fecha.
         */
        if (mostrarCalendario) {

            val fechaInicial =
                convertirTextoAFecha(
                    fechaEntrega
                )

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

                                onFechaEntregaChange(
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

        // --------------------------------------------------------
        // NOTAS
        // --------------------------------------------------------

        OutlinedTextField(
            value = notas,
            onValueChange =
                onNotasChange,
            modifier =
                Modifier.fillMaxWidth(),
            label = {
                Text("Notas (opcional)")
            },
            minLines = 2,
            maxLines = 4,
            enabled = !guardando,
            shape =
                RoundedCornerShape(12.dp)
        )

        // --------------------------------------------------------
        // ACCIONES DEL FORMULARIO
        // --------------------------------------------------------

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            if (
                trabajoEditandoId != null
            ) {

                OutlinedButton(
                    onClick =
                        onCancelarEdicion,
                    modifier =
                        Modifier.weight(1f),
                    enabled = !guardando,
                    shape =
                        RoundedCornerShape(12.dp)
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Close,
                        contentDescription = null
                    )

                    Spacer(
                        modifier =
                            Modifier.width(6.dp)
                    )

                    Text("Cancelar")
                }
            }

            Button(
                onClick = onGuardar,
                modifier =
                    Modifier
                        .weight(1f)
                        .height(52.dp),
                enabled =
                    !guardando &&
                            clientes.isNotEmpty(),
                shape =
                    RoundedCornerShape(12.dp)
            ) {

                if (guardando) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier
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

                    Icon(
                        imageVector =
                            if (
                                trabajoEditandoId == null
                            ) {
                                Icons.Default.Save
                            } else {
                                Icons.Default.Edit
                            },
                        contentDescription = null
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text(
                        if (
                            trabajoEditandoId == null
                        ) {
                            "Registrar trabajo"
                        } else {
                            "Guardar cambios"
                        }
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta individual de un trabajo.
 */
@Composable
private fun TrabajoItem(
    trabajo: TrabajoListado,
    onEditar: () -> Unit,
    onCambiarEstado: (String) -> Unit
) {

    // Formateamos el importe únicamente para presentación.
    val importe = remember(
        trabajo.importeCentavos
    ) {
        formatoMoneda(
            trabajo.importeCentavos
        )
    }

    /*
     * Define únicamente la apariencia visual del estado.
     */
    val colorEstado =
        when (trabajo.estado) {

            "PENDIENTE" ->
                MaterialTheme.colorScheme.secondary

            "EN_PROCESO" ->
                MaterialTheme.colorScheme.primary

            "TERMINADO" ->
                MaterialTheme.colorScheme.tertiary

            "ENTREGADO" ->
                MaterialTheme.colorScheme.primary

            "CANCELADO" ->
                MaterialTheme.colorScheme.error

            else ->
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        }

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            // --------------------------------------------------------
            // FOLIO + ESTADO
            // --------------------------------------------------------

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            trabajo.folio,
                        style =
                            MaterialTheme.typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            trabajo.nombreCliente,
                        style =
                            MaterialTheme.typography
                                .bodyMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }

                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                colorEstado.copy(
                                    alpha = 0.12f
                                )
                        ),
                    shape =
                        RoundedCornerShape(8.dp)
                ) {

                    Text(
                        text =
                            trabajo.estado,
                        modifier =
                            Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            ),
                        color =
                            colorEstado,
                        style =
                            MaterialTheme.typography
                                .labelMedium,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            HorizontalDivider()

            // --------------------------------------------------------
            // DESCRIPCIÓN
            // --------------------------------------------------------

            Text(
                text =
                    trabajo.descripcion,
                style =
                    MaterialTheme.typography.bodyLarge
            )

            // --------------------------------------------------------
            // IMPORTE
            // --------------------------------------------------------

            Text(
                text =
                    "Importe: $importe",
                style =
                    MaterialTheme.typography.titleSmall,
                fontWeight =
                    FontWeight.SemiBold
            )

            // --------------------------------------------------------
            // FECHA DE RECEPCIÓN
            // --------------------------------------------------------

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Event,
                    contentDescription = null,
                    modifier =
                        Modifier.width(20.dp),
                    tint =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.width(6.dp)
                )

                Text(
                    text =
                        "Recepción: ${
                            formatearFecha(
                                trabajo.fechaRecepcion
                            )
                        }",
                    style =
                        MaterialTheme.typography
                            .bodySmall
                )
            }

            // --------------------------------------------------------
            // ENTREGA ESTIMADA
            // --------------------------------------------------------

            trabajo.fechaEntregaEstimada?.let {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Schedule,
                        contentDescription = null,
                        modifier =
                            Modifier.width(20.dp),
                        tint =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    Spacer(
                        modifier =
                            Modifier.width(6.dp)
                    )

                    Text(
                        text =
                            "Entrega estimada: ${
                                formatearFecha(it)
                            }",
                        style =
                            MaterialTheme.typography
                                .bodySmall
                    )
                }
            }

            // --------------------------------------------------------
            // ENTREGA REAL
            // --------------------------------------------------------

            trabajo.fechaEntregaReal?.let {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier =
                            Modifier.width(20.dp),
                        tint =
                            MaterialTheme.colorScheme
                                .primary
                    )

                    Spacer(
                        modifier =
                            Modifier.width(6.dp)
                    )

                    Text(
                        text =
                            "Entregado: ${
                                formatearFecha(it)
                            }",
                        style =
                            MaterialTheme.typography
                                .bodySmall
                    )
                }
            }

            // --------------------------------------------------------
            // BOTÓN EDITAR
            // --------------------------------------------------------

            if (trabajo.estado != "ENTREGADO") {

                OutlinedButton(
                    onClick = onEditar,
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(10.dp)
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Edit,
                        contentDescription = null
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text("Editar trabajo")
                }
            }

            // --------------------------------------------------------
            // ESTADOS
            // --------------------------------------------------------

            when (trabajo.estado) {

                "PENDIENTE" -> {

                    Button(
                        onClick = {
                            onCambiarEstado(
                                "EN_PROCESO"
                            )
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        shape =
                            RoundedCornerShape(10.dp)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.PlayArrow,
                            contentDescription = null
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )

                        Text("Iniciar trabajo")
                    }
                }

                "EN_PROCESO" -> {

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        Button(
                            onClick = {
                                onCambiarEstado(
                                    "TERMINADO"
                                )
                            },
                            modifier =
                                Modifier.weight(1f),
                            shape =
                                RoundedCornerShape(10.dp)
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.CheckCircle,
                                contentDescription = null
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(6.dp)
                            )

                            Text("Terminado")
                        }

                        OutlinedButton(
                            onClick = {
                                onCambiarEstado(
                                    "CANCELADO"
                                )
                            },
                            modifier =
                                Modifier.weight(1f),
                            shape =
                                RoundedCornerShape(10.dp),
                            colors =
                                ButtonDefaults
                                    .outlinedButtonColors(
                                        contentColor =
                                            MaterialTheme
                                                .colorScheme
                                                .error
                                    )
                        ) {

                            Text("Cancelar")
                        }
                    }
                }

                "TERMINADO" -> {

                    Button(
                        onClick = {
                            onCambiarEstado(
                                "ENTREGADO"
                            )
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        shape =
                            RoundedCornerShape(10.dp)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.CheckCircle,
                            contentDescription = null
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )

                        Text("Registrar entrega")
                    }

                    OutlinedButton(
                        onClick = {
                            onCambiarEstado(
                                "CANCELADO"
                            )
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        shape =
                            RoundedCornerShape(10.dp),
                        colors =
                            ButtonDefaults
                                .outlinedButtonColors(
                                    contentColor =
                                        MaterialTheme
                                            .colorScheme
                                            .error
                                )
                    ) {

                        Text("Cancelar trabajo")
                    }
                }

                "CANCELADO" -> {

                    /*
                     * Un cancelado puede volver al flujo normal.
                     * Esto no modifica ni elimina sus pagos.
                     */
                    Text(
                        text =
                            "Este trabajo está cancelado. " +
                                    "Puedes reactivarlo.",
                        style =
                            MaterialTheme.typography
                                .bodySmall,
                        color =
                            MaterialTheme.colorScheme.error
                    )

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        OutlinedButton(
                            onClick = {
                                onCambiarEstado(
                                    "PENDIENTE"
                                )
                            },
                            modifier =
                                Modifier.weight(1f),
                            shape =
                                RoundedCornerShape(10.dp)
                        ) {

                            Text("Pendiente")
                        }

                        OutlinedButton(
                            onClick = {
                                onCambiarEstado(
                                    "EN_PROCESO"
                                )
                            },
                            modifier =
                                Modifier.weight(1f),
                            shape =
                                RoundedCornerShape(10.dp)
                        ) {

                            Text("En proceso")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            onCambiarEstado(
                                "TERMINADO"
                            )
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        shape =
                            RoundedCornerShape(10.dp)
                    ) {

                        Text(
                            "Marcar como terminado"
                        )
                    }
                }

                "ENTREGADO" -> {

                    Text(
                        text =
                            "Trabajo entregado. " +
                                    "El estado es definitivo.",
                        style =
                            MaterialTheme.typography
                                .bodySmall,
                        color =
                            MaterialTheme.colorScheme
                                .primary
                    )
                }
            }
        }
    }
}

/**
 * Formatea una fecha almacenada como timestamp.
 */
private fun formatearFecha(
    fecha: Long
): String {

    return SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    ).format(
        Date(fecha)
    )
}

/**
 * Formatea centavos como moneda mexicana.
 */
private fun formatoMoneda(
    centavos: Long
): String {

    return String.format(
        Locale("es", "MX"),
        "$%,.2f MXN",
        centavos / 100.0
    )
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
