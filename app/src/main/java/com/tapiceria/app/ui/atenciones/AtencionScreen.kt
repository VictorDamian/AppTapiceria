package com.tapiceria.app.ui.atenciones

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tapiceria.app.data.local.entity.ClienteEntity
import com.tapiceria.app.domain.model.AtencionListado
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla para registrar, editar y consultar atenciones.
 */
@Composable
fun AtencionScreen(
    viewModel: AtencionViewModel
) {
    val estado by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {

            Text(
                text = "Atenciones",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Registra consultas y solicitudes de cotización.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {

            estado.error?.let { mensaje ->

                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            estado.mensaje?.let { mensaje ->

                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {

            FormularioAtencion(
                clientes = estado.clientes,
                clienteSeleccionadoId =
                    estado.clienteSeleccionadoId,
                textoBusquedaCliente =
                    estado.textoBusquedaCliente,
                tipoSeleccionado =
                    estado.tipoSeleccionado,
                descripcion =
                    estado.descripcion,
                notas =
                    estado.notas,
                atencionEditandoId =
                    estado.atencionEditandoId,
                guardando =
                    estado.guardando,
                onBusquedaClienteChange =
                    viewModel::cambiarBusquedaCliente,
                onSeleccionarCliente =
                    viewModel::seleccionarCliente,
                onSeleccionarSinCliente =
                    viewModel::seleccionarSinCliente,
                onSeleccionarTipo =
                    viewModel::seleccionarTipo,
                onDescripcionChange =
                    viewModel::cambiarDescripcion,
                onNotasChange =
                    viewModel::cambiarNotas,
                onGuardar =
                    viewModel::guardarAtencion,
                onCancelarEdicion =
                    viewModel::cancelarEdicion
            )
        }

        item {

            Text(
                text = "Historial de atenciones",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (estado.cargando) {

            item {
                CircularProgressIndicator()
            }

        } else if (estado.atenciones.isEmpty()) {

            item {

                Text(
                    text = "Todavía no hay atenciones registradas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        } else {

            items(
                items = estado.atenciones,
                key = { it.id }
            ) { atencion ->

                AtencionItem(
                    atencion = atencion,
                    onEditar = {
                        viewModel.editarAtencion(atencion.id)
                    }
                )
            }
        }
    }
}

/**
 * Formulario para crear o editar una atención.
 */
@Composable
private fun FormularioAtencion(
    clientes: List<ClienteEntity>,
    clienteSeleccionadoId: Long?,
    textoBusquedaCliente: String,
    tipoSeleccionado: String,
    descripcion: String,
    notas: String,
    atencionEditandoId: Long?,
    guardando: Boolean,
    onBusquedaClienteChange: (String) -> Unit,
    onSeleccionarCliente: (Long) -> Unit,
    onSeleccionarSinCliente: () -> Unit,
    onSeleccionarTipo: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onNotasChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelarEdicion: () -> Unit
) {

    val clientesFiltrados = clientes
        .filter { cliente ->

            // En nuevas atenciones solamente se muestran
            // clientes activos.
            val disponible =
                cliente.activo ||
                        cliente.id == clienteSeleccionadoId

            if (!disponible) {
                return@filter false
            }

            val texto =
                textoBusquedaCliente.trim()

            texto.isBlank() ||
                    cliente.nombre.contains(
                        texto,
                        ignoreCase = true
                    ) ||
                    cliente.telefono.contains(
                        texto,
                        ignoreCase = true
                    )
        }
        .take(10)

    val clienteSeleccionado =
        clientes.firstOrNull {
            it.id == clienteSeleccionadoId
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text =
                    if (atencionEditandoId == null) {
                        "Nueva atención"
                    } else {
                        "Editar atención"
                    },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            /**
             * Cliente seleccionado actualmente.
             */
            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            ) {

                Text(
                    text =
                        clienteSeleccionado?.nombre
                            ?: "Pendiente / No aplica"
                )
            }

            /**
             * Campo de búsqueda.
             */
            OutlinedTextField(
                value = textoBusquedaCliente,
                onValueChange =
                    onBusquedaClienteChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Buscar cliente")
                },
                placeholder = {
                    Text("Nombre o teléfono")
                },
                singleLine = true,
                enabled = !guardando
            )

            /**
             * Opción para dejar la atención sin cliente.
             */
            OutlinedButton(
                onClick = onSeleccionarSinCliente,
                modifier = Modifier.fillMaxWidth(),
                enabled = !guardando
            ) {
                Text("Pendiente / No aplica")
            }

            /**
             * Resultados de búsqueda.
             */
            clientesFiltrados.forEach { cliente ->

                OutlinedButton(
                    onClick = {
                        onSeleccionarCliente(cliente.id)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !guardando && cliente.activo
                ) {

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = cliente.nombre,
                            fontWeight = FontWeight.Medium
                        )

                        if (cliente.telefono.isNotBlank()) {

                            Text(
                                text = cliente.telefono,
                                style =
                                    MaterialTheme.typography.bodySmall,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }

                        if (!cliente.activo) {

                            Text(
                                text = "Cliente inactivo",
                                style =
                                    MaterialTheme.typography.bodySmall,
                                color =
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Text(
                text = "Tipo de atención",
                style = MaterialTheme.typography.labelLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = {
                        onSeleccionarTipo("CONSULTA")
                    },
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
                    onClick = {
                        onSeleccionarTipo("COTIZACION")
                    },
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
                label = {
                    Text("Descripción")
                },
                placeholder = {
                    Text(
                        "Ej. Reparación de sillón de tres plazas"
                    )
                },
                minLines = 2,
                maxLines = 4,
                enabled = !guardando
            )

            OutlinedTextField(
                value = notas,
                onValueChange = onNotasChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Notas (opcional)")
                },
                minLines = 2,
                maxLines = 4,
                enabled = !guardando
            )

            Button(
                onClick = onGuardar,
                modifier = Modifier.fillMaxWidth(),
                enabled = !guardando
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

                    Text(
                        if (atencionEditandoId == null) {
                            "Registrar atención"
                        } else {
                            "Guardar cambios"
                        }
                    )
                }
            }

            /**
             * El botón solamente aparece durante una edición.
             */
            if (atencionEditandoId != null) {

                TextButton(
                    onClick = onCancelarEdicion,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !guardando
                ) {
                    Text("Cancelar edición")
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
    atencion: AtencionListado,
    onEditar: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
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
                    text = atencion.nombreCliente,
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = onEditar
                ) {
                    Text("Editar")
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color =
                    MaterialTheme.colorScheme.primaryContainer
            ) {

                Text(
                    text =
                        if (atencion.tipo == "COTIZACION") {
                            "Solicitud de cotización"
                        } else {
                            "Consulta"
                        },
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                    color =
                        MaterialTheme.colorScheme
                            .onPrimaryContainer,
                    style =
                        MaterialTheme.typography.labelMedium
                )
            }

            Text(
                text = atencion.descripcion,
                style = MaterialTheme.typography.bodyLarge
            )

            if (atencion.notas.isNotBlank()) {

                Text(
                    text = "Notas: ${atencion.notas}",
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }

            HorizontalDivider()

            Text(
                text =
                    "Fecha: ${formatearFecha(atencion.fechaAtencion)}",
                style =
                    MaterialTheme.typography.labelMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

/**
 * Convierte la fecha almacenada en milisegundos
 * a un formato legible.
 */
private fun formatearFecha(
    fecha: Long
): String {

    val formato = SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale.getDefault()
    )

    return formato.format(Date(fecha))
}