package com.tapiceria.app.ui.clientes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tapiceria.app.data.local.entity.ClienteEntity

/**
 * Pantalla principal de gestión de clientes.
 *
 * Permite registrar, editar y desactivar clientes, además de consultar
 * el historial de cada uno.
 */
@Composable
fun ClienteScreen(
    viewModel: ClienteViewModel,
    onVerHistorial: (Long) -> Unit
) {
    val estado by viewModel.uiState.collectAsState()

    var mostrarFormulario by remember { mutableStateOf(false) }
    var clienteEditar by remember { mutableStateOf<ClienteEntity?>(null) }
    var clienteDesactivar by remember { mutableStateOf<ClienteEntity?>(null) }

    // Filtra localmente los clientes observados por Room.
    val clientesFiltrados = remember(
        estado.clientes,
        estado.textoBusqueda
    ) {
        val texto = estado.textoBusqueda.trim()

        if (texto.isBlank()) {
            estado.clientes
        } else {
            estado.clientes.filter { cliente ->
                cliente.nombre.contains(texto, ignoreCase = true) ||
                        cliente.telefono.contains(texto, ignoreCase = true)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    clienteEditar = null
                    mostrarFormulario = true
                    viewModel.limpiarMensajes()
                }
            ) {
                Text("+")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Clientes",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = estado.textoBusqueda,
                onValueChange = viewModel::cambiarBusqueda,
                label = { Text("Buscar por nombre o teléfono") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mostrar mensajes de operación y errores.
            estado.mensaje?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = viewModel::limpiarMensajes) {
                    Text("Cerrar mensaje")
                }
            }

            estado.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(onClick = viewModel::limpiarMensajes) {
                    Text("Cerrar error")
                }
            }

            when {
                estado.cargando -> {
                    CircularProgressIndicator()
                }

                clientesFiltrados.isEmpty() -> {
                    Text(
                        text = if (estado.textoBusqueda.isBlank()) {
                            "Todavía no hay clientes registrados."
                        } else {
                            "No se encontraron clientes."
                        },
                        modifier = Modifier.padding(vertical = 16.dp)
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
                                onDesactivar = {
                                    clienteDesactivar = cliente
                                },
                                onVerHistorial = {
                                    // Envía el ID del cliente seleccionado.
                                    onVerHistorial(cliente.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // El mismo formulario se utiliza para registrar y editar.
    if (mostrarFormulario) {
        ClienteFormDialog(
            cliente = clienteEditar,
            onCerrar = {
                mostrarFormulario = false
                clienteEditar = null
            },
            onGuardar = { nombre, telefono, direccion, notas ->
                viewModel.guardarCliente(
                    id = clienteEditar?.id ?: 0L,
                    nombre = nombre,
                    telefono = telefono,
                    direccion = direccion,
                    notas = notas
                )

                mostrarFormulario = false
                clienteEditar = null
            }
        )
    }

    // La desactivación requiere confirmación explícita.
    clienteDesactivar?.let { cliente ->
        AlertDialog(
            onDismissRequest = { clienteDesactivar = null },
            title = { Text("Desactivar cliente") },
            text = {
                Text(
                    "¿Deseas desactivar a ${cliente.nombre}? " +
                            "Se conservará su historial."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.desactivarCliente(cliente.id)
                        clienteDesactivar = null
                    }
                ) {
                    Text("Desactivar")
                }
            },
            dismissButton = {
                TextButton(onClick = { clienteDesactivar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/** Tarjeta con los datos principales y las acciones del cliente. */
@Composable
private fun ClienteCard(
    cliente: ClienteEntity,
    onEditar: () -> Unit,
    onDesactivar: () -> Unit,
    onVerHistorial: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = cliente.nombre,
                style = MaterialTheme.typography.titleMedium
            )

            if (cliente.telefono.isNotBlank()) {
                Text("Teléfono: ${cliente.telefono}")
            }

            if (cliente.direccion.isNotBlank()) {
                Text("Dirección: ${cliente.direccion}")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onEditar) {
                    Text("Editar")
                }

                OutlinedButton(onClick = onVerHistorial) {
                    Text("Ver historial")
                }

                TextButton(onClick = onDesactivar) {
                    Text("Desactivar")
                }
            }
        }
    }
}

/**
 * Formulario reutilizable para altas y ediciones.
 * Los campos se inicializan con los datos existentes al editar.
 */
@Composable
private fun ClienteFormDialog(
    cliente: ClienteEntity?,
    onCerrar: () -> Unit,
    onGuardar: (String, String, String, String) -> Unit
) {
    var nombre by remember(cliente?.id) {
        mutableStateOf(cliente?.nombre.orEmpty())
    }
    var telefono by remember(cliente?.id) {
        mutableStateOf(cliente?.telefono.orEmpty())
    }
    var direccion by remember(cliente?.id) {
        mutableStateOf(cliente?.direccion.orEmpty())
    }
    var notas by remember(cliente?.id) {
        mutableStateOf(cliente?.notas.orEmpty())
    }
    var errorNombre by remember { mutableStateOf(false) }

    // evita que el formulario se cierre cuando el teléfono tenga un formato incorrecto.
    var errorTelefono by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = {
            Text(if (cliente == null) "Nuevo cliente" else "Editar cliente")
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
                    label = { Text("Nombre *") },
                    isError = errorNombre,
                    supportingText = {
                        if (errorNombre) {
                            Text("El nombre es obligatorio.")
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
                    label = { Text("Teléfono") },
                    isError = errorTelefono,
                    supportingText = {
                        if (errorTelefono) {
                            Text("Usa entre 7 y 20 caracteres válidos.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Primero valida los campos para no cerrar el formulario con errores.
                    val telefonoValido = telefono.isBlank() ||
                            telefono.matches(Regex("^[0-9+()\\-\\s]{7,20}$"))

                    errorNombre = nombre.isBlank()
                    errorTelefono = !telefonoValido

                    if (!errorNombre && telefonoValido) {
                        onGuardar(nombre, telefono, direccion, notas)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cancelar")
            }
        }
    )
}