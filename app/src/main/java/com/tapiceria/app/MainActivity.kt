package com.tapiceria.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        // Inicializa el contenedor de repositorios de la aplicación.
        container = (application as TapiceriaApplication).container

        setContent {
            MaterialTheme {
                // Cada ViewModel se crea una sola vez para esta pantalla.
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

                var pantallaActual by remember {
                    mutableStateOf("CLIENTES")
                }

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Navegación temporal; posteriormente la integraremos
                    // en una barra de navegación más completa.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (pantallaActual == "CLIENTES") {
                            Button(
                                onClick = { pantallaActual = "CLIENTES" },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Clientes")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { pantallaActual = "CLIENTES" },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Clientes")
                            }
                        }

                        if (pantallaActual == "ATENCIONES") {
                            Button(
                                onClick = { pantallaActual = "ATENCIONES" },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Atenciones")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { pantallaActual = "ATENCIONES" },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Atenciones")
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        when (pantallaActual) {
                            "CLIENTES" -> ClienteScreen(
                                viewModel = clienteViewModel
                            )

                            "ATENCIONES" -> AtencionScreen(
                                viewModel = atencionViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}