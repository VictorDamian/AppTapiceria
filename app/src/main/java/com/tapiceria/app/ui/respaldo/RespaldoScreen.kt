
package com.tapiceria.app.ui.respaldo

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tapiceria.app.data.respaldo.RespaldoService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Interfaz para crear respaldos y preparar restauraciones.
 */
@Composable
fun RespaldoScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val servicio = remember {
        RespaldoService(context.applicationContext)
    }

    var mensaje by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf(false) }
    var procesando by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    // El selector de Android determina dónde guardar el ZIP.
    val selectorGuardar = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { destino ->
        if (destino == null) {
            mensaje = "Creación del respaldo cancelada."
            error = false
        } else {
            scope.launch {
                procesando = true
                mensaje = null

                try {
                    servicio.crearRespaldo(destino)
                    mensaje = "Respaldo creado correctamente."
                    error = false
                } catch (ex: Exception) {
                    mensaje = ex.message ?: "No se pudo crear el respaldo."
                    error = true
                } finally {
                    procesando = false
                }
            }
        }
    }

    // El selector permite elegir un ZIP del almacenamiento o de otra ubicación.
    val selectorRestaurar = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { origen ->
        if (origen == null) {
            mensaje = "Restauración cancelada."
            error = false
        } else {
            scope.launch {
                procesando = true
                mensaje = null

                try {
                    servicio.prepararRestauracion(origen)

                    mensaje =
                        "Respaldo validado y preparado. Para aplicarlo, " +
                                "fuerza la detención de TapiceriaApp desde Ajustes " +
                                "del teléfono y vuelve a abrirla. No registres datos " +
                                "hasta que se haya reiniciado."
                    error = false
                    mostrarConfirmacion = true
                } catch (ex: Exception) {
                    mensaje = ex.message ?: "No se pudo preparar la restauración."
                    error = true
                } finally {
                    procesando = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Conserva las secciones de crear y restaurar respaldo.

        Text(
            text = "Respaldo y restauración",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Guarda la información de la tapicería y sus fotografías " +
                    "en un ZIP para recuperarlas cuando sea necesario.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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
            // Conserva el título, la descripción y el botón actuales.

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Crear respaldo",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    "Incluye la base de datos y las fotografías guardadas " +
                            "en el almacenamiento privado de la aplicación."
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !procesando,
                    onClick = {
                        val fecha = SimpleDateFormat(
                            "yyyyMMdd_HHmm",
                            Locale.getDefault()
                        ).format(Date())

                        selectorGuardar.launch("Respaldo_Tapiceria_$fecha.zip")
                    }
                ) {
                    Text("Crear respaldo ZIP")
                }
            }
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
            // Conserva el título, la descripción y el botón actuales.

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Restaurar respaldo",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    "Selecciona un respaldo ZIP de TapiceriaApp. " +
                            "La restauración reemplazará los datos locales " +
                            "actuales cuando se reinicie la aplicación."
                )

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !procesando,
                    onClick = {
                        selectorRestaurar.launch(
                            arrayOf("application/zip", "application/octet-stream")
                        )
                    }
                ) {
                    Text("Seleccionar ZIP para restaurar")
                }
            }
        }

        if (procesando) {
            Text("Procesando archivo...")
        }

        mensaje?.let {
            Text(
                text = it,
                color = if (error) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Restauración preparada") },
            text = {
                Text(
                    "La restauración aún no se ha aplicado. " +
                            "Fuerza la detención de la aplicación desde los " +
                            "ajustes del dispositivo y vuelve a abrirla. " +
                            "Al iniciar, se intentará aplicar el respaldo."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { mostrarConfirmacion = false }
                ) {
                    Text("Entendido")
                }
            }
        )
    }
}