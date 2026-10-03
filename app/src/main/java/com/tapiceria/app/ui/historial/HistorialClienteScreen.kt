
package com.tapiceria.app.ui.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.tapiceria.app.data.local.entity.AtencionEntity
import com.tapiceria.app.data.local.entity.CotizacionEntity
import com.tapiceria.app.data.local.entity.FotoTrabajoEntity
import com.tapiceria.app.data.local.entity.PagoEntity
import com.tapiceria.app.data.local.entity.TrabajoEntity
import java.io.File
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
// Ventana ampliada y controles de la fotografía.
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close

// Gestos para ampliar, reducir y mover la fotografía.
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Pantalla de consulta del historial de un cliente.
 *
 * No modifica registros. Toda la información proviene del ViewModel.
 */
@Composable
fun HistorialClienteScreen(
    viewModel: HistorialClienteViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    val historial = estado.historial

    val formatoMoneda = NumberFormat.getCurrencyInstance(
        Locale("es", "MX")
    )

    // Convierte los centavos almacenados en la base de datos a moneda.
    fun moneda(centavos: Long): String {
        return formatoMoneda.format(centavos / 100.0)
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = onVolver,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text("Volver")
            }
        }

        Text(
            text = "Historial del cliente",
            style = MaterialTheme.typography.headlineSmall
        )

        if (estado.cargando) {
            CircularProgressIndicator()
        } else if (estado.error != null) {
            Text(
                text = estado.error.orEmpty(),
                color = MaterialTheme.colorScheme.error
            )
        } else {
            val cliente = historial.cliente

            if (cliente != null) {
                // Datos generales y contacto.
                SeccionHistorial(titulo = "Datos del cliente") {
                    TextoDato("Nombre", cliente.nombre)
                    TextoDato("Teléfono", cliente.telefono)
                    TextoDato("Dirección", cliente.direccion)
                    TextoDato("Notas", cliente.notas)
                }

                // Resumen financiero.
                SeccionHistorial(titulo = "Resumen de pagos") {
                    TextoDato(
                        "Total abonado",
                        moneda(historial.totalPagadoCentavos)
                    )
                    TextoDato(
                        "Saldo pendiente estimado",
                        moneda(historial.saldoPendienteCentavos)
                    )
                }

                SeccionHistorial(
                    titulo = "Consultas (${historial.atenciones.count { it.tipo == "CONSULTA" }})"
                ) {
                    val consultas = historial.atenciones.filter {
                        it.tipo == "CONSULTA"
                    }

                    if (consultas.isEmpty()) {
                        TextoVacio("No hay consultas registradas.")
                    } else {
                        consultas.forEach { atencion ->
                            TarjetaAtencion(atencion)
                        }
                    }
                }

                SeccionHistorial(
                    titulo = "Cotizaciones (${historial.cotizaciones.size})"
                ) {
                    if (historial.cotizaciones.isEmpty()) {
                        TextoVacio("No hay cotizaciones registradas.")
                    } else {
                        historial.cotizaciones.forEach { cotizacion ->
                            TarjetaCotizacion(
                                cotizacion = cotizacion,
                                moneda = ::moneda
                            )
                        }
                    }
                }

                SeccionHistorial(
                    titulo = "Trabajos (${historial.trabajos.size})"
                ) {
                    if (historial.trabajos.isEmpty()) {
                        TextoVacio("No hay trabajos registrados.")
                    } else {
                        // Muestra cada trabajo junto con sus pagos y su saldo pendiente.
                        // Cada tarjeta recibe los pagos correspondientes exclusivamente a ese trabajo.
                        historial.trabajos.forEach { trabajo ->
                            TarjetaTrabajo(
                                trabajo = trabajo,
                                pagos = historial.pagos.filter { pago ->
                                    pago.trabajoId == trabajo.id
                                },
                                totalPagadoCentavos = historial.totalPagadoTrabajoCentavos(trabajo.id),
                                saldoPendienteCentavos = historial.saldoTrabajoCentavos(trabajo.id),
                                moneda = ::moneda
                            )
                        }
                    }
                }

                SeccionHistorial(
                    titulo = "Pagos (${historial.pagos.size})"
                ) {
                    if (historial.pagos.isEmpty()) {
                        TextoVacio("No hay pagos registrados.")
                    } else {
                        historial.pagos.forEach { pago ->
                            TarjetaPago(
                                pago = pago,
                                moneda = ::moneda
                            )
                        }
                    }
                }

                SeccionHistorial(
                    titulo = "Fotografías (${historial.fotografias.size})"
                ) {
                    if (historial.fotografias.isEmpty()) {
                        TextoVacio("No hay fotografías registradas.")
                    } else {
                        historial.fotografias.forEach { foto ->
                            TarjetaFotografia(foto)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Contenedor reutilizable para las secciones del historial.
 * El contenido y sus operaciones permanecen sin cambios.
 */
@Composable
private fun SeccionHistorial(
    titulo: String,
    contenido: @Composable () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            contenido()
        }
    }
}

@Composable
private fun TextoDato(etiqueta: String, valor: String?) {
    if (!valor.isNullOrBlank()) {
        Text(
            text = "$etiqueta: $valor",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun TextoVacio(mensaje: String) {
    Text(
        text = mensaje,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun TarjetaAtencion(atencion: AtencionEntity) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(atencion.descripcion)
        TextoDato("Fecha", atencion.fechaAtencion.toString())
        TextoDato("Notas", atencion.notas)
        HorizontalDivider()
    }
}

@Composable
private fun TarjetaCotizacion(
    cotizacion: CotizacionEntity,
    moneda: (Long) -> String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = cotizacion.folio,
            style = MaterialTheme.typography.titleSmall
        )
        Text(cotizacion.descripcion)
        TextoDato("Importe", moneda(cotizacion.importeCentavos))
        TextoDato("Estado", cotizacion.estado)
        TextoDato("Vigencia", cotizacion.fechaVigencia.toString())
        HorizontalDivider()
    }
}

/**
 * Muestra los datos de un trabajo y permite consultar su situación financiera.
 */
@Composable
private fun TarjetaTrabajo(
    trabajo: TrabajoEntity,
    pagos: List<PagoEntity>,
    totalPagadoCentavos: Long,
    saldoPendienteCentavos: Long,
    moneda: (Long) -> String
) {
    // Controla la visibilidad del detalle financiero.
    var mostrarDetalle by remember(trabajo.id) {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        // Conserva el contenido original de la tarjeta.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Información general del trabajo.
            Text(
                text = "Trabajo ${trabajo.folio}",
                style = MaterialTheme.typography.titleMedium
            )

            TextoDato(
                etiqueta = "Descripción",
                valor = trabajo.descripcion
            )

            TextoDato(
                etiqueta = "Estado",
                valor = trabajo.estado.toString()
            )

            TextoDato(
                etiqueta = "Fecha de recepción",
                valor = trabajo.fechaRecepcion.toString()
            )

            TextoDato(
                etiqueta = "Entrega estimada",
                valor = trabajo.fechaEntregaEstimada.toString()
            )

            // Resumen financiero del trabajo.
            HorizontalDivider()

            TextoDato(
                etiqueta = "Importe del trabajo",
                valor = moneda(trabajo.importeCentavos)
            )

            TextoDato(
                etiqueta = "Total pagado",
                valor = moneda(totalPagadoCentavos)
            )

            TextoDato(
                etiqueta = "Saldo pendiente",
                valor = moneda(saldoPendienteCentavos)
            )

            // Permite expandir o contraer los pagos del trabajo.
            TextButton(
                onClick = {
                    mostrarDetalle = !mostrarDetalle
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = if (mostrarDetalle) {
                        "Ocultar pagos"
                    } else {
                        "Ver pagos (${pagos.size})"
                    }
                )
            }

            // El detalle solo se compone cuando el usuario lo solicita.
            if (mostrarDetalle) {
                HorizontalDivider()

                Text(
                    text = "Detalle de pagos",
                    style = MaterialTheme.typography.titleSmall
                )

                if (pagos.isEmpty()) {
                    TextoVacio(
                        mensaje = "Este trabajo todavía no tiene pagos registrados."
                    )
                } else {
                    // Muestra primero los pagos más recientes.
                    pagos.sortedByDescending { it.fechaPago }
                        .forEach { pago ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TextoDato(
                                    etiqueta = "Importe",
                                    valor = moneda(pago.importeCentavos)
                                )

                                TextoDato(
                                    etiqueta = "Fecha",
                                    valor = pago.fechaPago.toString()
                                )

                                TextoDato(
                                    etiqueta = "Método",
                                    valor = pago.metodo.toString()
                                )

                                if (!pago.referencia.isNullOrBlank()) {
                                    TextoDato(
                                        etiqueta = "Referencia",
                                        valor = pago.referencia
                                    )
                                }

                                if (!pago.notas.isNullOrBlank()) {
                                    TextoDato(
                                        etiqueta = "Notas",
                                        valor = pago.notas
                                    )
                                }

                                HorizontalDivider()
                            }
                        }
                }
            }
        }
    }
}

@Composable
private fun TarjetaPago(
    pago: PagoEntity,
    moneda: (Long) -> String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = moneda(pago.importeCentavos),
            style = MaterialTheme.typography.titleSmall
        )
        TextoDato("Fecha", pago.fechaPago.toString())
        TextoDato("Método", pago.metodo)
        TextoDato("Referencia", pago.referencia)
        TextoDato("Notas", pago.notas)
        HorizontalDivider()
    }
}

/**
 * Muestra una fotografía del trabajo y permite abrirla en una vista ampliada.
 */
@Composable
private fun TarjetaFotografia(
    foto: FotoTrabajoEntity
) {
    val context = LocalContext.current

    // Las fotografías se guardan en el almacenamiento privado de la aplicación.
    val archivoFoto = remember(foto.rutaArchivo) {
        File(context.filesDir, foto.rutaArchivo)
    }

    // Controla la apertura de la vista ampliada.
    var mostrarImagenAmpliada by remember(foto.rutaArchivo) {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        // Conserva el contenido original de la tarjeta.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Fotografía ${foto.tipo}",
                style = MaterialTheme.typography.titleMedium
            )

            // La imagen se puede tocar para abrirla en grande.
            AsyncImage(
                model = archivoFoto,
                contentDescription = "Fotografía ${foto.tipo} del trabajo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        if (archivoFoto.exists()) {
                            mostrarImagenAmpliada = true
                        }
                    },
                contentScale = ContentScale.Fit
            )

            if (!foto.descripcion.isNullOrBlank()) {
                TextoDato(
                    etiqueta = "Descripción",
                    valor = foto.descripcion
                )
            }

            TextoDato(
                etiqueta = "Fecha de registro",
                valor = foto.fechaRegistro.toString()
            )
        }
    }

    // La ventana se muestra solamente cuando el usuario selecciona la imagen.
    if (mostrarImagenAmpliada) {
        Dialog(
            onDismissRequest = {
                mostrarImagenAmpliada = false
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            ImagenTrabajoAmpliada(
                archivo = archivoFoto,
                rotacionGrados = foto.rotacionGrados,
                titulo = "Fotografía ${foto.tipo}",
                onCerrar = {
                    mostrarImagenAmpliada = false
                }
            )
        }
    }
}

/**
 * Presenta la fotografía en una ventana amplia con gestos de zoom y desplazamiento.
 */
@Composable
private fun ImagenTrabajoAmpliada(
    archivo: File,
    rotacionGrados: Int,
    titulo: String,
    onCerrar: () -> Unit
) {
    // El estado se reinicia al abrir otra fotografía.
    var escala by remember(archivo.absolutePath) {
        mutableStateOf(1f)
    }

    var desplazamientoX by remember(archivo.absolutePath) {
        mutableStateOf(0f)
    }

    var desplazamientoY by remember(archivo.absolutePath) {
        mutableStateOf(0f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(12.dp)
    ) {
        // Encabezado con el nombre y el botón para cerrar.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = titulo,
                modifier = Modifier.weight(1f),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = onCerrar) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar fotografía",
                    tint = Color.White
                )
            }
        }

        // El usuario puede pellizcar para hacer zoom y arrastrar para desplazarse.
        AsyncImage(
            model = archivo,
            contentDescription = titulo,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .pointerInput(archivo.absolutePath) {
                    detectTransformGestures { _, desplazamiento, factorZoom, _ ->
                        escala = (escala * factorZoom).coerceIn(1f, 5f)

                        // El desplazamiento solo se aplica cuando la imagen está ampliada.
                        if (escala > 1f) {
                            desplazamientoX += desplazamiento.x
                            desplazamientoY += desplazamiento.y
                        } else {
                            desplazamientoX = 0f
                            desplazamientoY = 0f
                        }
                    }
                }
                .graphicsLayer {
                    scaleX = escala
                    scaleY = escala
                    translationX = desplazamientoX
                    translationY = desplazamientoY
                    rotationZ = rotacionGrados.toFloat()
                },
            contentScale = ContentScale.Fit
        )

        // Restablece el zoom para volver a ver la imagen completa.
        TextButton(
            onClick = {
                escala = 1f
                desplazamientoX = 0f
                desplazamientoY = 0f
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Restablecer zoom", color = Color.White)
        }
    }
}