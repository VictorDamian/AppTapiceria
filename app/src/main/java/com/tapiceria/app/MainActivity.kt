
package com.tapiceria.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
 * Actividad principal con navegación entre los módulos implementados.
 */
class MainActivity : ComponentActivity() {

    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Reutiliza el contenedor de dependencias de la aplicación.
        container = (application as TapiceriaApplication).container

        setContent {
            TapiceriaDamianTheme() {
                val clienteViewModel: ClienteViewModel = viewModel(
                    factory = ClienteViewModelFactory(
                        container.clienteRepository
                    )
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

                // ViewModel para registrar y dar seguimiento a los trabajos.
                val trabajoViewModel: TrabajoViewModel = viewModel(
                    factory = TrabajoViewModelFactory(
                        container.trabajoRepository,
                        container.clienteRepository
                    )
                )

                // ViewModel del módulo de fotografías.
                val fotoTrabajoViewModel: FotoTrabajoViewModel = viewModel(
                    factory = FotoTrabajoViewModelFactory(
                        context = applicationContext,
                        trabajoRepository = container.trabajoRepository,
                        fotoTrabajoRepository = container.fotoTrabajoRepository
                    )
                )

                // ViewModel del módulo de pagos.
                val pagoViewModel: PagoViewModel = viewModel(
                    factory = PagoViewModelFactory(
                        trabajoRepository = container.trabajoRepository,
                        pagoRepository = container.pagoRepository
                    )
                )

                // ViewModel del Dashboard, conectado al repositorio de indicadores.
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModelFactory(
                        dashboardRepository = container.dashboardRepository
                    )
                )

                var pantallaActual by remember {
                    mutableStateOf("INICIO")
                }

                // Conserva el ID del cliente seleccionado para abrir su historial.
                var clienteIdHistorial by remember {
                    mutableStateOf<Long?>(null)
                }

                // Se utiliza una clave diferente para cada cliente, de modo que
                // cada historial tenga su propio ViewModel.
                val idParaHistorial = clienteIdHistorial ?: 0L

                val historialClienteViewModel: HistorialClienteViewModel = viewModel(
                    key = "historial_cliente_$idParaHistorial",
                    factory = HistorialClienteViewModelFactory(
                        clienteId = idParaHistorial,
                        repository = container.historialClienteRepository
                    )
                )

                Column(modifier = Modifier.fillMaxSize()) {

                    // Barra de navegación con fondo y elevación para separarla del contenido.
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Se conserva la navegación existente.

                            BotonNavegacion(
                                texto = "Inicio",
                                seleccionado = pantallaActual == "INICIO"
                            ) {
                                pantallaActual = "INICIO"
                            }

                            BotonNavegacion(
                                texto = "Clientes",
                                seleccionado = pantallaActual == "CLIENTES"
                            ) {
                                pantallaActual = "CLIENTES"
                            }

                            BotonNavegacion(
                                texto = "Atenciones",
                                seleccionado = pantallaActual == "ATENCIONES"
                            ) {
                                pantallaActual = "ATENCIONES"
                            }

                            BotonNavegacion(
                                texto = "Cotizaciones",
                                seleccionado = pantallaActual == "COTIZACIONES"
                            ) {
                                pantallaActual = "COTIZACIONES"
                            }

                            BotonNavegacion(
                                texto = "Trabajos",
                                seleccionado = pantallaActual == "TRABAJOS"
                            ) {
                                pantallaActual = "TRABAJOS"
                            }

                            BotonNavegacion(
                                texto = "Fotos",
                                seleccionado = pantallaActual == "FOTOGRAFIAS"
                            ) {
                                pantallaActual = "FOTOGRAFIAS"
                            }

                            BotonNavegacion(
                                texto = "Pagos",
                                seleccionado = pantallaActual == "PAGOS"
                            ) {
                                pantallaActual = "PAGOS"
                            }

                            BotonNavegacion(
                                texto = "Exportar",
                                seleccionado = pantallaActual == "EXPORTAR"
                            ) {
                                pantallaActual = "EXPORTAR"
                            }

                            BotonNavegacion(
                                texto = "Respaldo",
                                seleccionado = pantallaActual == "RESPALDO"
                            ) {
                                pantallaActual = "RESPALDO"
                            }
                        }
                    }

                    // Muestra únicamente el módulo seleccionado.
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        when (pantallaActual) {

                            "CLIENTES" -> ClienteScreen(
                                viewModel = clienteViewModel,
                                onVerHistorial = { clienteId ->
                                    // Guarda el cliente seleccionado y abre su historial.
                                    clienteIdHistorial = clienteId
                                    pantallaActual = "HISTORIAL"
                                }
                            )

                            "HISTORIAL" -> {
                                HistorialClienteScreen(
                                    viewModel = historialClienteViewModel,
                                    onVolver = {
                                        // Regresa a la lista sin perder los filtros de búsqueda.
                                        pantallaActual = "CLIENTES"
                                    }
                                )
                            }

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
 * Botón reutilizable para evitar repetir el diseño de navegación.
 */
@Composable
private fun BotonNavegacion(
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