package com.tapiceria.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tapiceria.app.di.AppContainer
import com.tapiceria.app.ui.atenciones.AtencionScreen
import com.tapiceria.app.ui.atenciones.AtencionViewModel
import com.tapiceria.app.ui.atenciones.AtencionViewModelFactory
import com.tapiceria.app.ui.clientes.ClienteScreen
import com.tapiceria.app.ui.clientes.ClienteViewModel
import com.tapiceria.app.ui.clientes.ClienteViewModelFactory
import com.tapiceria.app.ui.cotizaciones.CotizacionScreen
import com.tapiceria.app.ui.cotizaciones.CotizacionViewModel
import com.tapiceria.app.ui.cotizaciones.CotizacionViewModelFactory
import com.tapiceria.app.ui.dashboard.DashboardScreen
import com.tapiceria.app.ui.dashboard.DashboardViewModel
import com.tapiceria.app.ui.dashboard.DashboardViewModelFactory
import com.tapiceria.app.ui.exportacion.ExportacionScreen
import com.tapiceria.app.ui.fotografias.FotoTrabajoScreen
import com.tapiceria.app.ui.fotografias.FotoTrabajoViewModel
import com.tapiceria.app.ui.fotografias.FotoTrabajoViewModelFactory
import com.tapiceria.app.ui.historial.HistorialClienteScreen
import com.tapiceria.app.ui.historial.HistorialClienteViewModel
import com.tapiceria.app.ui.historial.HistorialClienteViewModelFactory
import com.tapiceria.app.ui.pagos.PagoScreen
import com.tapiceria.app.ui.pagos.PagoViewModel
import com.tapiceria.app.ui.pagos.PagoViewModelFactory
import com.tapiceria.app.ui.respaldo.RespaldoScreen
import com.tapiceria.app.ui.theme.TapiceriaDamianTheme
import com.tapiceria.app.ui.trabajos.TrabajoScreen
import com.tapiceria.app.ui.trabajos.TrabajoViewModel
import com.tapiceria.app.ui.trabajos.TrabajoViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        container = (application as TapiceriaApplication).container

        setContent {
            TapiceriaDamianTheme {
                val clienteViewModel: ClienteViewModel = viewModel(
                    factory = ClienteViewModelFactory(container.clienteRepository)
                )

                val atencionViewModel: AtencionViewModel = viewModel(
                    factory = AtencionViewModelFactory(
                        container.atencionRepository,
                        container.clienteRepository
                    )
                )

                val cotizacionViewModel: CotizacionViewModel = viewModel(
                    factory = CotizacionViewModelFactory(
                        container.cotizacionRepository,
                        container.atencionRepository
                    )
                )

                val trabajoViewModel: TrabajoViewModel = viewModel(
                    factory = TrabajoViewModelFactory(
                        container.trabajoRepository,
                        container.clienteRepository
                    )
                )

                val fotoTrabajoViewModel: FotoTrabajoViewModel = viewModel(
                    factory = FotoTrabajoViewModelFactory(
                        context = applicationContext,
                        trabajoRepository = container.trabajoRepository,
                        fotoTrabajoRepository = container.fotoTrabajoRepository
                    )
                )

                val pagoViewModel: PagoViewModel = viewModel(
                    factory = PagoViewModelFactory(
                        trabajoRepository = container.trabajoRepository,
                        pagoRepository = container.pagoRepository
                    )
                )

                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModelFactory(
                        dashboardRepository = container.dashboardRepository
                    )
                )

                var pantallaActual by remember { mutableStateOf("INICIO") }
                var clienteIdHistorial by remember { mutableStateOf<Long?>(null) }

                val idParaHistorial = clienteIdHistorial ?: 0L

                val historialClienteViewModel: HistorialClienteViewModel = viewModel(
                    key = "historial_cliente_$idParaHistorial",
                    factory = HistorialClienteViewModelFactory(
                        clienteId = idParaHistorial,
                        repository = container.historialClienteRepository
                    )
                )

                Column(modifier = Modifier.fillMaxSize()) {

                    // Contenedor de la navegación dividida
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            // --- SECCIÓN 1: Accesos principales (Arriba: 4 botones) ---
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BotonNavegacionItem(
                                    texto = "Inicio",
                                    icono = Icons.Default.Home,
                                    seleccionado = pantallaActual == "INICIO",
                                    modifier = Modifier.weight(1f)
                                ) { pantallaActual = "INICIO" }

                                BotonNavegacionItem(
                                    texto = "Fotos",
                                    icono = Icons.Default.CameraAlt,
                                    seleccionado = pantallaActual == "FOTOGRAFIAS",
                                    modifier = Modifier.weight(1f)
                                ) { pantallaActual = "FOTOGRAFIAS" }

                                BotonNavegacionItem(
                                    texto = "Exportar",
                                    icono = Icons.Default.FileDownload,
                                    seleccionado = pantallaActual == "EXPORTAR",
                                    modifier = Modifier.weight(1f)
                                ) { pantallaActual = "EXPORTAR" }

                                BotonNavegacionItem(
                                    texto = "Respaldo",
                                    icono = Icons.Default.Backup,
                                    seleccionado = pantallaActual == "RESPALDO",
                                    modifier = Modifier.weight(1f)
                                ) { pantallaActual = "RESPALDO" }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            // --- SECCIÓN 2: Barra de navegación inferior con Scroll ---
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BotonNavegacionItem(
                                    texto = "Clientes",
                                    icono = Icons.Default.People,
                                    seleccionado = pantallaActual == "CLIENTES"
                                ) { pantallaActual = "CLIENTES" }

                                BotonNavegacionItem(
                                    texto = "Atenciones",
                                    icono = Icons.Default.SupportAgent,
                                    seleccionado = pantallaActual == "ATENCIONES"
                                ) { pantallaActual = "ATENCIONES" }

                                BotonNavegacionItem(
                                    texto = "Cotizaciones",
                                    icono = Icons.Default.Receipt,
                                    seleccionado = pantallaActual == "COTIZACIONES"
                                ) { pantallaActual = "COTIZACIONES" }

                                BotonNavegacionItem(
                                    texto = "Trabajos",
                                    icono = Icons.Default.Build,
                                    seleccionado = pantallaActual == "TRABAJOS"
                                ) { pantallaActual = "TRABAJOS" }

                                BotonNavegacionItem(
                                    texto = "Pagos",
                                    icono = Icons.Default.MonetizationOn,
                                    seleccionado = pantallaActual == "PAGOS"
                                ) { pantallaActual = "PAGOS" }
                            }
                        }
                    }

                    // --- Módulo seleccionado ---
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        when (pantallaActual) {
                            "CLIENTES" -> ClienteScreen(
                                viewModel = clienteViewModel,
                                onVerHistorial = { clienteId ->
                                    clienteIdHistorial = clienteId
                                    pantallaActual = "HISTORIAL"
                                }
                            )
                            "HISTORIAL" -> HistorialClienteScreen(
                                viewModel = historialClienteViewModel,
                                onVolver = { pantallaActual = "CLIENTES" }
                            )
                            "ATENCIONES" -> AtencionScreen(atencionViewModel)
                            "COTIZACIONES" -> CotizacionScreen(cotizacionViewModel)
                            "TRABAJOS" -> TrabajoScreen(trabajoViewModel)
                            "FOTOGRAFIAS" -> FotoTrabajoScreen(fotoTrabajoViewModel)
                            "PAGOS" -> PagoScreen(pagoViewModel)
                            "INICIO" -> DashboardScreen(dashboardViewModel)
                            "EXPORTAR" -> ExportacionScreen()
                            "RESPALDO" -> RespaldoScreen()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Botón con ícono vertical destacado y respuesta al estado seleccionado.
 */
@Composable
private fun BotonNavegacionItem(
    texto: String,
    icono: ImageVector,
    seleccionado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = if (seleccionado) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val contentColor = if (seleccionado) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = texto,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = texto,
                fontSize = 12.sp,
                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}