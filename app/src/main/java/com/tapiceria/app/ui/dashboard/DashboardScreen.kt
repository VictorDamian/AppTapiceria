
package com.tapiceria.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Pantalla principal con indicadores y resúmenes del negocio.
 */
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val estado by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Resumen del negocio",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Consulta rápida de tus trabajos y cobros.",
            style = MaterialTheme.typography.bodyMedium
        )

        if (estado.cargando) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        estado.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Indicadores principales.
        Text(
            text = "Indicadores",
            style = MaterialTheme.typography.titleLarge
        )

        TarjetaIndicador(
            titulo = "Trabajos activos",
            cantidad = estado.resumen.trabajosActivos.toString(),
            detalle = "Pendientes, en proceso o terminados por entregar"
        )

        TarjetaIndicador(
            titulo = "Cotizaciones pendientes",
            cantidad = estado.resumen.cotizacionesPendientes.toString(),
            detalle = "Esperando respuesta del cliente"
        )

        TarjetaIndicador(
            titulo = "Entregas en los próximos 7 días",
            cantidad = estado.resumen.entregasProximas.toString(),
            detalle = "Según la fecha estimada registrada"
        )

        TarjetaIndicador(
            titulo = "Trabajos con saldo",
            cantidad = estado.resumen.trabajosConSaldo.toString(),
            detalle = "Trabajos no cancelados ni entregados"
        )

        // Listado de los trabajos más recientes.
        TituloSeccion("Últimos trabajos")

        if (estado.trabajosRecientes.isEmpty() && !estado.cargando) {
            TextoVacio("Todavía no hay trabajos registrados.")
        }

        estado.trabajosRecientes.forEach { trabajo ->
            TarjetaTrabajo(trabajo)
        }

        // Cotizaciones que todavía esperan respuesta.
        TituloSeccion("Cotizaciones pendientes")

        if (estado.cotizacionesPendientes.isEmpty() && !estado.cargando) {
            TextoVacio("No hay cotizaciones pendientes.")
        }

        estado.cotizacionesPendientes.forEach { cotizacion ->
            TarjetaCotizacion(cotizacion)
        }

        // Entregas estimadas dentro de los siguientes siete días.
        TituloSeccion("Próximas entregas")

        if (estado.entregasProximas.isEmpty() && !estado.cargando) {
            TextoVacio("No hay entregas previstas para los próximos siete días.")
        }

        estado.entregasProximas.forEach { entrega ->
            TarjetaEntrega(entrega)
        }

        // Trabajos que aún tienen saldo por cobrar.
        TituloSeccion("Saldos pendientes")

        if (estado.saldosPendientes.isEmpty() && !estado.cargando) {
            TextoVacio("No hay saldos pendientes.")
        }

        estado.saldosPendientes.forEach { saldo ->
            TarjetaSaldo(saldo)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun TarjetaIndicador(
    titulo: String,
    cantidad: String,
    detalle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)

            Text(
                cantidad,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                detalle,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun TituloSeccion(titulo: String) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleLarge
    )
}

@Composable
private fun TextoVacio(mensaje: String) {
    Text(
        text = mensaje,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun TarjetaTrabajo(trabajo: TrabajoDashboard) {
    val fecha = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${trabajo.folio} · ${trabajo.nombreCliente}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(trabajo.descripcion)
            Text("Estado: ${trabajo.estado}")
            Text("Importe: ${formatoMoneda(trabajo.importeCentavos)}")

            trabajo.fechaEntregaEstimada?.let {
                Text("Entrega estimada: ${fecha.format(Date(it))}")
            }
        }
    }
}

@Composable
private fun TarjetaCotizacion(
    cotizacion: CotizacionPendienteDashboard
) {
    val fecha = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${cotizacion.folio} · ${cotizacion.nombreCliente}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(cotizacion.descripcion)
            Text("Importe: ${formatoMoneda(cotizacion.importeCentavos)}")
            Text("Creada: ${fecha.format(Date(cotizacion.fechaCreacion))}")

            cotizacion.fechaVigencia?.let {
                Text("Vigencia: ${fecha.format(Date(it))}")
            }
        }
    }
}

@Composable
private fun TarjetaEntrega(entrega: EntregaProximaDashboard) {
    val fecha = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${entrega.folio} · ${entrega.nombreCliente}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Entrega: ${fecha.format(Date(entrega.fechaEntregaEstimada))}")
                Text("Estado: ${entrega.estado}")
            }
        }
    }
}

@Composable
private fun TarjetaSaldo(saldo: SaldoPendienteDashboard) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${saldo.folio} · ${saldo.nombreCliente}",
                style = MaterialTheme.typography.titleMedium
            )
            Text("Importe: ${formatoMoneda(saldo.importeCentavos)}")
            Text("Pagado: ${formatoMoneda(saldo.totalPagadoCentavos)}")
            Text(
                text = "Pendiente: ${formatoMoneda(saldo.saldoPendienteCentavos)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Presenta centavos como moneda mexicana.
 */
private fun formatoMoneda(centavos: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        .format(centavos / 100.0)
}