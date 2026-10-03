
package com.tapiceria.app.ui.exportacion

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tapiceria.app.data.exportacion.ExportacionService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment

/**
 * Pantalla de exportación manual de datos a CSV.
 */
@Composable
fun ExportacionScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val servicio = remember { ExportacionService(context.applicationContext) }

    var tablaSeleccionada by remember { mutableStateOf("clientes") }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var exportando by remember { mutableStateOf(false) }

    val selectorDestino = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) {
            mensaje = "Exportación cancelada."
        } else {
            scope.launch {
                exportando = true
                mensaje = null

                try {
                    servicio.exportarTablaCsv(tablaSeleccionada, uri)
                    mensaje = "Archivo CSV exportado correctamente."
                } catch (ex: Exception) {
                    mensaje = ex.message ?: "No se pudo exportar el archivo."
                } finally {
                    exportando = false
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
        // Conserva el contenido actual de ExportacionScreen.
        Text(
            text = "Exportar información",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Selecciona una tabla para generar un archivo CSV que " +
                    "puedes abrir con Excel.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        servicio.tablasExportables.forEach { tabla ->
            // Cada tabla exportable se presenta como una tarjeta independiente.
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icono decorativo; no modifica la operación de exportación.
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = etiquetaTabla(tabla),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = tabla,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        enabled = !exportando,
                        onClick = {
                            // Conservamos el nombre del archivo y el selector actuales.
                            tablaSeleccionada = tabla

                            val fecha = SimpleDateFormat(
                                "yyyyMMdd_HHmm",
                                Locale.getDefault()
                            ).format(Date())

                            selectorDestino.launch("${tabla}_$fecha.csv")
                        }
                    ) {
                        Text("Exportar")
                    }
                }
            }
        }

        if (exportando) {
            Text("Generando archivo...")
        }

        mensaje?.let {
            Text(
                text = it,
                color = if (it.startsWith("No se pudo") ||
                    it.startsWith("No se encontró")
                ) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}

/**
 * Convierte los nombres técnicos en etiquetas amigables.
 */
private fun etiquetaTabla(tabla: String): String {
    return when (tabla) {
        "clientes" -> "Clientes"
        "atenciones" -> "Atenciones"
        "cotizaciones" -> "Cotizaciones"
        "trabajos" -> "Trabajos"
        "pagos" -> "Pagos"
        "fotos_trabajo" -> "Fotografías de trabajos"
        "servicios" -> "Servicios"
        else -> tabla
    }
}