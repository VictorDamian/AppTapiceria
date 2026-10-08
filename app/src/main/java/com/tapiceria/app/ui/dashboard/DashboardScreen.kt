package com.tapiceria.app.ui.dashboard

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tapiceria.app.domain.model.CotizacionPendienteDashboard
import com.tapiceria.app.domain.model.EntregaProximaDashboard
import com.tapiceria.app.domain.model.SaldoPendienteDashboard
import com.tapiceria.app.domain.model.TrabajoDashboard
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla principal con los indicadores del negocio.
 *
 * Esta actualización modifica únicamente la presentación.
 * Se conserva el ViewModel y el flujo de información existente.
 */
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val estado by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Encabezado principal.
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Panel de control",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Resumen de la actividad",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Conserva el indicador de carga original.
        if (estado.cargando) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp
                )
            }
        }

        // Muestra los errores sin ocultar el resto de la pantalla.
        estado.error?.let { error ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Indicadores principales del negocio.
        TituloSeccion(
            titulo = "Indicadores",
            subtitulo = "Estado general de la operación"
        )

        TarjetaIndicador(
            titulo = "Trabajos activos",
            cantidad = estado.resumen.trabajosActivos.toString(),
            detalle = "Pendientes, en proceso o por entregar"
        )

        TarjetaIndicador(
            titulo = "Cotizaciones pendientes",
            cantidad = estado.resumen.cotizacionesPendientes.toString(),
            detalle = "Esperando respuesta del cliente"
        )

        TarjetaIndicador(
            titulo = "Entregas próximas",
            cantidad = estado.resumen.entregasProximas.toString(),
            detalle = "Entregas estimadas en los próximos 7 días"
        )

        TarjetaIndicador(
            titulo = "Trabajos con saldo",
            cantidad = estado.resumen.trabajosConSaldo.toString(),
            detalle = "Trabajos que todavía tienen importe pendiente"
        )

        // Trabajos recientes.
        TituloSeccion(
            titulo = "Últimos trabajos",
            subtitulo = "Actividad reciente del taller"
        )

        if (estado.trabajosRecientes.isEmpty() && !estado.cargando) {
            TextoVacio("Todavía no hay trabajos registrados.")
        }

        estado.trabajosRecientes.forEach { trabajo ->
            TarjetaTrabajo(trabajo)
        }

        // Cotizaciones pendientes de respuesta.
        TituloSeccion(
            titulo = "Cotizaciones pendientes",
            subtitulo = "Seguimiento de propuestas"
        )

        if (estado.cotizacionesPendientes.isEmpty() && !estado.cargando) {
            TextoVacio("No hay cotizaciones pendientes.")
        }

        estado.cotizacionesPendientes.forEach { cotizacion ->
            TarjetaCotizacion(cotizacion)
        }

        // Próximas entregas.
        TituloSeccion(
            titulo = "Próximas entregas",
            subtitulo = "Planificación de los próximos siete días"
        )

        if (estado.entregasProximas.isEmpty() && !estado.cargando) {
            TextoVacio("No hay entregas previstas para los próximos siete días.")
        }

        estado.entregasProximas.forEach { entrega ->
            TarjetaEntrega(entrega)
        }

        // Saldos por cobrar.
        TituloSeccion(
            titulo = "Saldos pendientes",
            subtitulo = "Importes que todavía faltan por cobrar"
        )

        if (estado.saldosPendientes.isEmpty() && !estado.cargando) {
            TextoVacio("No hay saldos pendientes.")
        }

        estado.saldosPendientes.forEach { saldo ->
            TarjetaSaldo(saldo)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Tarjeta reutilizable para los indicadores numéricos.
 */
@Composable
private fun TarjetaIndicador(
    titulo: String,
    cantidad: String,
    detalle: String
) {
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
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Acento visual para identificar rápidamente el indicador.
            Box(
                modifier = Modifier
                    .size(width = 5.dp, height = 58.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = cantidad,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Encabezado común para las secciones del dashboard.
 */
@Composable
private fun TituloSeccion(
    titulo: String,
    subtitulo: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = subtitulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Mensaje común cuando una sección no tiene información.
 */
@Composable
private fun TextoVacio(mensaje: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = mensaje,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Tarjeta de un trabajo reciente.
 */
@Composable
private fun TarjetaTrabajo(trabajo: TrabajoDashboard) {
    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = trabajo.folio,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = trabajo.nombreCliente,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = trabajo.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextoDatoDashboard(
                etiqueta = "Estado",
                valor = trabajo.estado
            )

            TextoDatoDashboard(
                etiqueta = "Importe",
                valor = formatoMoneda(trabajo.importeCentavos)
            )

            trabajo.fechaEntregaEstimada?.let { fecha ->
                TextoDatoDashboard(
                    etiqueta = "Entrega estimada",
                    valor = formatoFecha.format(Date(fecha))
                )
            }
        }
    }
}

/**
 * Tarjeta de una cotización pendiente.
 */
@Composable
private fun TarjetaCotizacion(
    cotizacion: CotizacionPendienteDashboard
) {
    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = cotizacion.folio,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = cotizacion.nombreCliente,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = cotizacion.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextoDatoDashboard(
                etiqueta = "Importe",
                valor = formatoMoneda(cotizacion.importeCentavos)
            )

            TextoDatoDashboard(
                etiqueta = "Creada",
                valor = formatoFecha.format(Date(cotizacion.fechaCreacion))
            )

            cotizacion.fechaVigencia?.let { fecha ->
                TextoDatoDashboard(
                    etiqueta = "Vigencia",
                    valor = formatoFecha.format(Date(fecha))
                )
            }
        }
    }
}

/**
 * Tarjeta de una entrega próxima.
 */
@Composable
private fun TarjetaEntrega(entrega: EntregaProximaDashboard) {
    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Marca visual de la sección de entregas.
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "E",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = entrega.folio,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = entrega.nombreCliente,
                    style = MaterialTheme.typography.bodyMedium
                )

                TextoDatoDashboard(
                    etiqueta = "Entrega",
                    valor = formatoFecha.format(Date(entrega.fechaEntregaEstimada))
                )

                TextoDatoDashboard(
                    etiqueta = "Estado",
                    valor = entrega.estado
                )
            }
        }
    }
}

/**
 * Tarjeta de un trabajo con saldo pendiente.
 */
@Composable
private fun TarjetaSaldo(saldo: SaldoPendienteDashboard) {
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = saldo.folio,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = saldo.nombreCliente,
                style = MaterialTheme.typography.bodyLarge
            )

            TextoDatoDashboard(
                etiqueta = "Importe",
                valor = formatoMoneda(saldo.importeCentavos)
            )

            TextoDatoDashboard(
                etiqueta = "Pagado",
                valor = formatoMoneda(saldo.totalPagadoCentavos)
            )

            // El saldo pendiente se destaca sin modificar su cálculo.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pendiente",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        text = formatoMoneda(saldo.saldoPendienteCentavos),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Presenta una etiqueta y su valor con una distribución uniforme.
 */
@Composable
private fun TextoDatoDashboard(
    etiqueta: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = etiqueta,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = valor,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Convierte los centavos a moneda mexicana.
 */
private fun formatoMoneda(centavos: Long): String {
    return NumberFormat
        .getCurrencyInstance(Locale("es", "MX"))
        .format(centavos / 100.0)
}