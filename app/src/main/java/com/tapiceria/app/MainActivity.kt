
package com.tapiceria.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
            MaterialTheme {
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

                var pantallaActual by remember {
                    mutableStateOf("CLIENTES")
                }

                Column(modifier = Modifier.fillMaxSize()) {

                    // Barra de navegación provisional.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BotonNavegacion(
                            texto = "Clientes",
                            seleccionado = pantallaActual == "CLIENTES",
                            modifier = Modifier.weight(1f)
                        ) {
                            pantallaActual = "CLIENTES"
                        }

                        BotonNavegacion(
                            texto = "Atenciones",
                            seleccionado = pantallaActual == "ATENCIONES",
                            modifier = Modifier.weight(1f)
                        ) {
                            pantallaActual = "ATENCIONES"
                        }

                        BotonNavegacion(
                            texto = "Cotizaciones",
                            seleccionado = pantallaActual == "COTIZACIONES",
                            modifier = Modifier.weight(1f)
                        ) {
                            pantallaActual = "COTIZACIONES"
                        }

                        BotonNavegacion(
                            texto = "Trabajos",
                            seleccionado = pantallaActual == "TRABAJOS",
                            modifier = Modifier.weight(1f)
                        ) {
                            pantallaActual = "TRABAJOS"
                        }
                    }

                    // Muestra únicamente el módulo seleccionado.
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        when (pantallaActual) {
                            "CLIENTES" -> ClienteScreen(clienteViewModel)

                            "ATENCIONES" -> AtencionScreen(atencionViewModel)

                            "COTIZACIONES" -> CotizacionScreen(cotizacionViewModel)

                            "TRABAJOS" -> TrabajoScreen(trabajoViewModel)
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