
package com.tapiceria.app.ui.fotografias

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tapiceria.app.data.local.entity.FotoTrabajoEntity
import com.tapiceria.app.domain.model.TrabajoListado
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla para registrar y consultar fotografías de cada trabajo.
 */
@Composable
fun FotoTrabajoScreen(
    viewModel: FotoTrabajoViewModel
) {
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var menuTrabajosAbierto by remember { mutableStateOf(false) }
    var uriCaptura by remember { mutableStateOf<Uri?>(null) }
    var archivoTemporal by remember { mutableStateOf<File?>(null) }

    // Abre la cámara del sistema y recibe el resultado.
    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        val uri = uriCaptura
        val archivo = archivoTemporal

        if (exito && uri != null) {
            // El ViewModel copia la imagen a su ubicación definitiva.
            viewModel.registrarFotografia(uri, archivo)
        } else {
            // Si se cancela la captura, elimina el archivo temporal.
            archivo?.delete()
        }

        uriCaptura = null
        archivoTemporal = null
    }

    // Permite seleccionar una imagen desde la galería o proveedor de fotos.
    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.registrarFotografia(it) }
    }

    val trabajoSeleccionado = estado.trabajos.firstOrNull {
        it.id == estado.trabajoSeleccionadoId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Fotografías",
            style = MaterialTheme.typography.headlineMedium
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

        if (estado.cargando) {
            CircularProgressIndicator()
        } else if (estado.trabajos.isEmpty()) {
            Text("Primero registra un trabajo para agregar fotografías.")
        } else {
            // Selector de trabajo.
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { menuTrabajosAbierto = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !estado.guardando
                ) {
                    Text(
                        trabajoSeleccionado?.let {
                            "${it.folio} - ${it.nombreCliente}"
                        } ?: "Seleccionar trabajo"
                    )
                }

                DropdownMenu(
                    expanded = menuTrabajosAbierto,
                    onDismissRequest = {
                        menuTrabajosAbierto = false
                    }
                ) {
                    estado.trabajos.forEach { trabajo ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(trabajo.folio)
                                    Text(
                                        text = trabajo.nombreCliente,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            onClick = {
                                viewModel.seleccionarTrabajo(trabajo.id)
                                menuTrabajosAbierto = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "Tipo de fotografía",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotonTipoFoto(
                    texto = "ANTES",
                    seleccionado = estado.tipoSeleccionado == "ANTES",
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.seleccionarTipo("ANTES")
                }

                BotonTipoFoto(
                    texto = "DESPUÉS",
                    seleccionado = estado.tipoSeleccionado == "DESPUES",
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.seleccionarTipo("DESPUES")
                }
            }

            OutlinedTextField(
                value = estado.descripcion,
                onValueChange = viewModel::cambiarDescripcion,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descripción (opcional)") },
                placeholder = {
                    Text("Ej. Vista frontal del sillón")
                },
                enabled = !estado.guardando,
                singleLine = true
            )

            Button(
                onClick = {
                    try {
                        val carpeta = File(
                            context.cacheDir,
                            "capturas"
                        )

                        if (!carpeta.exists() && !carpeta.mkdirs()) {
                            throw IllegalStateException(
                                "No fue posible crear la carpeta temporal."
                            )
                        }

                        val archivo = File.createTempFile(
                            "captura_",
                            ".jpg",
                            carpeta
                        )

                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            archivo
                        )

                        archivoTemporal = archivo
                        uriCaptura = uri
                        camaraLauncher.launch(uri)
                    } catch (_: Exception) {
                        archivoTemporal?.delete()
                        archivoTemporal = null
                        uriCaptura = null
                        // El mensaje de interfaz se mantiene en el ViewModel.
                        viewModel.limpiarMensaje()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !estado.guardando
            ) {
                Text("Tomar fotografía")
            }

            OutlinedButton(
                onClick = {
                    galeriaLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !estado.guardando
            ) {
                Text("Seleccionar de galería")
            }

            if (estado.guardando) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text("Guardando fotografía...")
                }
            }

            Text(
                text = "Fotografías del trabajo",
                style = MaterialTheme.typography.titleLarge
            )

            if (estado.fotografias.isEmpty()) {
                Text("Este trabajo todavía no tiene fotografías.")
            } else {
                // La pantalla ya tiene desplazamiento vertical; usamos
                // Column para evitar anidar otra lista desplazable.
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    estado.fotografias.forEach { foto ->
                        FotoTrabajoItem(
                            foto = foto,
                            context = context,
                            onEliminar = {
                                viewModel.eliminarFotografia(foto.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonTipoFoto(
    texto: String,
    seleccionado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (seleccionado) {
        Button(
            onClick = onClick,
            modifier = modifier
        ) {
            Text(texto)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
        ) {
            Text(texto)
        }
    }
}

/**
 * Muestra la imagen y sus metadatos.
 */
@Composable
private fun FotoTrabajoItem(
    foto: FotoTrabajoEntity,
    context: android.content.Context,
    onEliminar: () -> Unit
) {
    val archivo = remember(foto.rutaArchivo) {
        File(context.filesDir, foto.rutaArchivo)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (foto.tipo == "DESPUES") {
                    "DESPUÉS"
                } else {
                    "ANTES"
                },
                style = MaterialTheme.typography.titleMedium
            )

            if (archivo.exists()) {
                AsyncImage(
                    model = archivo,
                    contentDescription = foto.descripcion.ifBlank {
                        "Fotografía ${foto.tipo.lowercase(Locale.ROOT)}"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            } else {
                Text(
                    text = "El archivo de imagen no está disponible.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (foto.descripcion.isNotBlank()) {
                Text(foto.descripcion)
            }

            Text(
                text = "Fecha: ${
                    SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                    ).format(Date(foto.fechaRegistro))
                }",
                        style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "Rotación: ${foto.rotacionGrados}°",
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedButton(
                onClick = onEliminar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar fotografía")
            }
        }
    }
}