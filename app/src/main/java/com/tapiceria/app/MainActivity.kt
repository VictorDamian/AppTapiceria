package com.tapiceria.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
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

/**
 * Modelo para definir los elementos de la barra de navegación inferior.
 */
private data class ItemNavegacionInferior(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
)

class MainActivity : ComponentActivity() {

    private lateinit var container: AppContainer

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

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
                var menuDesplegableExpandido by remember { mutableStateOf(false) }

                val idParaHistorial = clienteIdHistorial ?: 0L

                val historialClienteViewModel: HistorialClienteViewModel = viewModel(
                    key = "historial_cliente_$idParaHistorial",
                    factory = HistorialClienteViewModelFactory(
                        clienteId = idParaHistorial,
                        repository = container.historialClienteRepository
                    )
                )

                // Lista de secciones principales para la barra inferior
                val elementosInferiores = listOf(
                    ItemNavegacionInferior("INICIO", "Inicio", Icons.Default.Home),
                    ItemNavegacionInferior("CLIENTES", "Clientes", Icons.Default.People),
                    ItemNavegacionInferior("ATENCIONES", "Atenciones", Icons.Default.SupportAgent),
                    ItemNavegacionInferior("COTIZACIONES", "Cotizaciones", Icons.Default.Receipt),
                    ItemNavegacionInferior("TRABAJOS", "Trabajos", Icons.Default.Build),
                    ItemNavegacionInferior("FOTOGRAFIAS", "Fotos", Icons.Default.CameraAlt),
                    ItemNavegacionInferior("PAGOS", "Pagos", Icons.Default.MonetizationOn)
                )

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(), // Ajusta el contenido cuando el teclado se despliega
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = when (pantallaActual) {
                                        "INICIO" -> "Inicio"
                                        "CLIENTES" -> "Clientes"
                                        "HISTORIAL" -> "Historial del Cliente"
                                        "ATENCIONES" -> "Atenciones"
                                        "COTIZACIONES" -> "Cotizaciones"
                                        "TRABAJOS" -> "Trabajos"
                                        "FOTOGRAFIAS" -> "Fotografías"
                                        "PAGOS" -> "Pagos"
                                        "EXPORTAR" -> "Exportar Datos"
                                        "RESPALDO" -> "Copia de Respaldo"
                                        else -> "Tapicería"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            actions = {
                                // Menú de tres puntos a la derecha
                                IconButton(onClick = { menuDesplegableExpandido = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Opciones adicionales"
                                    )
                                }

                                DropdownMenu(
                                    expanded = menuDesplegableExpandido,
                                    onDismissRequest = { menuDesplegableExpandido = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Exportar") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.FileDownload,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            menuDesplegableExpandido = false
                                            pantallaActual = "EXPORTAR"
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Respaldo") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Backup,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            menuDesplegableExpandido = false
                                            pantallaActual = "RESPALDO"
                                        }
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    bottomBar = {
                        // Barra de navegación inferior
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            elementosInferiores.forEach { item ->
                                val estaSeleccionado = pantallaActual == item.ruta
                                NavigationBarItem(
                                    selected = estaSeleccionado,
                                    onClick = { pantallaActual = item.ruta },
                                    icon = {
                                        Icon(
                                            imageVector = item.icono,
                                            contentDescription = item.titulo,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.titulo,
                                            fontSize = 8.sp // <-- ¡Aquí cambias el tamaño de la letra! (Ajusta a 10.sp, 11.sp, 12.sp, etc.)
                                        )
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    // Contenido según el módulo activo
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
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