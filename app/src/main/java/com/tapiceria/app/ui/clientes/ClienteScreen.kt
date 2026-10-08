package com.tapiceria.app.ui.clientes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import com.tapiceria.app.data.local.entity.ClienteEntity

/**
 * Pantalla principal de administración de clientes.
 *
 * Permite:
 * - Registrar clientes.
 * - Editar clientes.
 * - Buscar clientes.
 * - Activar/desactivar clientes.
 * - Consultar el historial.
 */
@Composable
fun ClienteScreen(
    viewModel: ClienteViewModel,
    onVerHistorial: (Long) -> Unit
) {
    val estado by viewModel.uiState.collectAsState()

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var clienteEditar by remember {
        mutableStateOf<ClienteEntity?>(null)
    }

    var clienteCambiarEstado by remember {
        mutableStateOf<ClienteEntity?>(null)
    }

    /*
     * La búsqueda se realiza en memoria porque la cantidad de clientes
     * mostrada en esta pantalla normalmente será manejable.
     */
    val clientesFiltrados = remember(
        estado.clientes,
        estado.textoBusqueda
    ) {
        val texto = estado.textoBusqueda.trim()

        if (texto.isBlank()) {
            estado.clientes
        } else {
            estado.clientes.filter { cliente ->
                cliente.nombre.contains(
                    texto,
                    ignoreCase = true
                ) ||
                        cliente.telefono.contains(
                            texto,
                            ignoreCase = true
                        )
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    clienteEditar = null
                    mostrarFormulario = true
                    viewModel.limpiarMensajes()
                }
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Text(
                text = "Registra y edita clientes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )


            OutlinedTextField(
                value = estado.textoBusqueda,
                onValueChange = viewModel::cambiarBusqueda,
                label = {
                    Text("Buscar por nombre o teléfono")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // Mensaje de operación exitosa.
            estado.mensaje?.let { mensaje ->

                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.primary
                )

                TextButton(
                    onClick = viewModel::limpiarMensajes
                ) {
                    Text("Cerrar mensaje")
                }
            }

            // Mensaje de error.
            estado.error?.let { error ->

                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )

                TextButton(
                    onClick = viewModel::limpiarMensajes
                ) {
                    Text("Cerrar error")
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            when {

                estado.cargando -> {
                    CircularProgressIndicator()
                }

                clientesFiltrados.isEmpty() -> {
                    Text(
                        text = if (
                            estado.textoBusqueda.isBlank()
                        ) {
                            "Todavía no hay clientes registrados."
                        } else {
                            "No se encontraron clientes."
                        },
                        modifier = Modifier.padding(
                            vertical = 16.dp
                        )
                    )
                }

                else -> {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        items(
                            items = clientesFiltrados,
                            key = { it.id }
                        ) { cliente ->

                            ClienteCard(
                                cliente = cliente,

                                onEditar = {
                                    clienteEditar = cliente
                                    mostrarFormulario = true
                                    viewModel.limpiarMensajes()
                                },

                                onCambiarEstado = {
                                    clienteCambiarEstado = cliente
                                },

                                onVerHistorial = {
                                    onVerHistorial(cliente.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    /*
     * Formulario para alta y edición.
     */
    if (mostrarFormulario) {

        ClienteFormDialog(
            cliente = clienteEditar,

            guardando = estado.guardando,

            onCerrar = {
                if (!estado.guardando) {
                    mostrarFormulario = false
                    clienteEditar = null
                }
            },

            onGuardar = {
                    nombre,
                    telefono,
                    direccion,
                    notas ->

                viewModel.guardarCliente(
                    id = clienteEditar?.id ?: 0L,
                    nombre = nombre,
                    telefono = telefono,
                    direccion = direccion,
                    notas = notas
                )

                /*
                 * El formulario se cerrará cuando la operación haya
                 * terminado correctamente mediante el siguiente estado.
                 *
                 * Para no cerrar prematuramente ante un error, se mantiene
                 * abierto mientras guardando sea true.
                 */
                if (!estado.guardando) {
                    mostrarFormulario = false
                    clienteEditar = null
                }
            }
        )
    }

    /*
     * Confirmación para activar/desactivar.
     */
    clienteCambiarEstado?.let { cliente ->

        val activo = cliente.activo

        AlertDialog(
            onDismissRequest = {
                if (!estado.guardando) {
                    clienteCambiarEstado = null
                }
            },

            title = {
                Text(
                    if (activo) {
                        "Desactivar cliente"
                    } else {
                        "Activar cliente"
                    }
                )
            },

            text = {
                Text(
                    if (activo) {
                        "¿Deseas desactivar a ${cliente.nombre}? " +
                                "Su historial se conservará."
                    } else {
                        "¿Deseas activar nuevamente a ${cliente.nombre}?"
                    }
                )
            },

            confirmButton = {

                TextButton(
                    enabled = !estado.guardando,

                    onClick = {
                        if (activo) {
                            viewModel.desactivarCliente(cliente.id)
                        } else {
                            viewModel.activarCliente(cliente.id)
                        }

                        clienteCambiarEstado = null
                    }
                ) {
                    Text(
                        if (activo) {
                            "Desactivar"
                        } else {
                            "Activar"
                        }
                    )
                }
            },

            dismissButton = {

                TextButton(
                    enabled = !estado.guardando,
                    onClick = {
                        clienteCambiarEstado = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Tarjeta visual de un cliente.
 */
@Composable
private fun ClienteCard(
    cliente: ClienteEntity,
    onEditar: () -> Unit,
    onCambiarEstado: () -> Unit,
    onVerHistorial: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = cliente.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (cliente.activo) {
                        "ACTIVO"
                    } else {
                        "INACTIVO"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (cliente.activo) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            if (cliente.telefono.isNotBlank()) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Teléfono",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = cliente.telefono,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (cliente.direccion.isNotBlank()) {

                Row(
                    verticalAlignment = Alignment.Top
                ) {

                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Dirección",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = cliente.direccion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider()

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                OutlinedButton(
                    onClick = onEditar
                ) {
                    Text("Editar")
                }

                Button(
                    onClick = onVerHistorial
                ) {
                    Text("Ver historial")
                }

                TextButton(
                    onClick = onCambiarEstado,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor =
                            if (cliente.activo) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                    )
                ) {
                    Text(
                        if (cliente.activo) {
                            "Desactivar"
                        } else {
                            "Activar"
                        }
                    )
                }
            }
        }
    }
}

/**
 * Formulario reutilizable para alta y edición.
 */
@Composable
private fun ClienteFormDialog(
    cliente: ClienteEntity?,
    guardando: Boolean,
    onCerrar: () -> Unit,
    onGuardar: (
        String,
        String,
        String,
        String
    ) -> Unit
) {

    var nombre by remember(cliente?.id) {
        mutableStateOf(
            cliente?.nombre.orEmpty()
        )
    }

    var telefono by remember(cliente?.id) {
        mutableStateOf(
            cliente?.telefono.orEmpty()
        )
    }

    var direccion by remember(cliente?.id) {
        mutableStateOf(
            cliente?.direccion.orEmpty()
        )
    }

    var notas by remember(cliente?.id) {
        mutableStateOf(
            cliente?.notas.orEmpty()
        )
    }

    var errorNombre by remember {
        mutableStateOf(false)
    }

    var errorTelefono by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = {
            if (!guardando) {
                onCerrar()
            }
        },

        title = {
            Text(
                if (cliente == null) {
                    "Nuevo cliente"
                } else {
                    "Editar cliente"
                }
            )
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    value = nombre,

                    onValueChange = {
                        nombre = it
                        errorNombre = false
                    },

                    label = {
                        Text("Nombre *")
                    },

                    isError = errorNombre,

                    supportingText = {
                        if (errorNombre) {
                            Text(
                                "El nombre es obligatorio."
                            )
                        }
                    },

                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = telefono,

                    onValueChange = {
                        telefono = it
                        errorTelefono = false
                    },

                    label = {
                        Text("Teléfono")
                    },

                    isError = errorTelefono,

                    supportingText = {
                        if (errorTelefono) {
                            Text(
                                "Usa entre 7 y 20 caracteres válidos."
                            )
                        }
                    },

                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = direccion,
                    onValueChange = {
                        direccion = it
                    },
                    label = {
                        Text("Dirección")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notas,
                    onValueChange = {
                        notas = it
                    },
                    label = {
                        Text("Notas")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },

        confirmButton = {

            Button(
                enabled = !guardando,

                onClick = {

                    val nombreValido =
                        nombre.isNotBlank()

                    val telefonoValido =
                        telefono.isBlank() ||
                                telefono.matches(
                                    Regex(
                                        "^[0-9+()\\-\\s]{7,20}$"
                                    )
                                )

                    errorNombre = !nombreValido
                    errorTelefono = !telefonoValido

                    if (
                        nombreValido &&
                        telefonoValido
                    ) {
                        onGuardar(
                            nombre,
                            telefono,
                            direccion,
                            notas
                        )
                    }
                }
            ) {

                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar")
                }
            }
        },

        dismissButton = {

            TextButton(
                enabled = !guardando,
                onClick = onCerrar
            ) {
                Text("Cancelar")
            }
        }
    )
}