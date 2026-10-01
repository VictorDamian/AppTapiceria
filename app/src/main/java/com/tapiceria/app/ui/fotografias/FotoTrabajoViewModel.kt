
package com.tapiceria.app.ui.fotografias

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tapiceria.app.data.local.entity.FotoTrabajoEntity
import com.tapiceria.app.domain.repository.FotoTrabajoRepository
import com.tapiceria.app.domain.repository.TrabajoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Administra las fotografías de cada trabajo.
 */
class FotoTrabajoViewModel(
    context: Context,
    private val trabajoRepository: TrabajoRepository,
    private val fotoRepository: FotoTrabajoRepository
) : ViewModel() {

    // Conserva únicamente el contexto de aplicación para evitar fugas.
    private val appContext = context.applicationContext

    private val _uiState = MutableStateFlow(FotoTrabajoUiState())
    val uiState: StateFlow<FotoTrabajoUiState> = _uiState.asStateFlow()

    private var fotosJob: Job? = null

    init {
        observarTrabajos()
    }

    /**
     * Carga los trabajos disponibles para seleccionar.
     */
    private fun observarTrabajos() {
        viewModelScope.launch {
            trabajoRepository.observarTodos()
                .catch {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = "No fue posible cargar los trabajos."
                        )
                    }
                }
                .collect { trabajos ->
                    val seleccionActual = _uiState.value.trabajoSeleccionadoId
                    val seleccionValida = trabajos.any {
                        it.id == seleccionActual
                    }

                    val nuevoId = if (seleccionValida) {
                        seleccionActual
                    } else {
                        trabajos.firstOrNull()?.id
                    }

                    _uiState.update {
                        it.copy(
                            trabajos = trabajos,
                            trabajoSeleccionadoId = nuevoId,
                            cargando = false
                        )
                    }

                    if (nuevoId != seleccionActual) {
                        observarFotografias(nuevoId)
                    }
                }
        }
    }

    fun seleccionarTrabajo(trabajoId: Long) {
        if (_uiState.value.trabajoSeleccionadoId == trabajoId) return

        _uiState.update {
            it.copy(
                trabajoSeleccionadoId = trabajoId,
                fotografias = emptyList(),
                error = null,
                mensaje = null
            )
        }

        observarFotografias(trabajoId)
    }

    /**
     * Cambia la clasificación de la fotografía.
     */
    fun seleccionarTipo(tipo: String) {
        if (tipo !in listOf("ANTES", "DESPUES")) return

        _uiState.update {
            it.copy(
                tipoSeleccionado = tipo,
                error = null,
                mensaje = null
            )
        }
    }

    fun cambiarDescripcion(valor: String) {
        _uiState.update {
            it.copy(
                descripcion = valor,
                error = null,
                mensaje = null
            )
        }
    }

    private fun observarFotografias(trabajoId: Long?) {
        fotosJob?.cancel()

        if (trabajoId == null) {
            _uiState.update { it.copy(fotografias = emptyList()) }
            return
        }

        fotosJob = viewModelScope.launch {
            fotoRepository.observarPorTrabajo(trabajoId)
                .catch {
                    _uiState.update {
                        it.copy(
                            error = "No fue posible cargar las fotografías."
                        )
                    }
                }
                .collect { fotografias ->
                    _uiState.update {
                        it.copy(fotografias = fotografias)
                    }
                }
        }
    }

    /**
     * Registra una imagen seleccionada o capturada.
     *
     * archivoTemporal solo se utiliza para las capturas creadas
     * por nuestra pantalla; las imágenes de galería no se eliminan.
     */
    fun registrarFotografia(
        origen: Uri,
        archivoTemporal: File? = null
    ) {
        val estado = _uiState.value
        val trabajoId = estado.trabajoSeleccionadoId

        if (estado.guardando) return

        if (trabajoId == null ||
            estado.trabajos.none { it.id == trabajoId }
        ) {
            mostrarError("Selecciona un trabajo.")
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(guardando = true, error = null, mensaje = null)
            }

            var archivoDestino: File? = null

            try {
                val rutaRelativa = withContext(Dispatchers.IO) {
                    val carpeta = File(
                        appContext.filesDir,
                        "photos/trabajos"
                    )

                    if (!carpeta.exists() && !carpeta.mkdirs()) {
                        throw IOException(
                            "No fue posible crear la carpeta de fotografías."
                        )
                    }

                    // El UUID evita colisiones entre nombres de archivo.
                    val archivo = File(
                        carpeta,
                        "${UUID.randomUUID()}.jpg"
                    )
                    archivoDestino = archivo

                    val entrada = appContext.contentResolver
                        .openInputStream(origen)
                        ?: throw IOException("No fue posible leer la imagen.")

                    entrada.use { input ->
                        archivo.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    if (archivo.length() == 0L) {
                        throw IOException("La imagen está vacía.")
                    }

                    "photos/trabajos/${archivo.name}"
                }

                val foto = FotoTrabajoEntity(
                    trabajoId = trabajoId,
                    tipo = estado.tipoSeleccionado,
                    rutaArchivo = rutaRelativa,
                    descripcion = estado.descripcion.trim()
                )

                fotoRepository.insertar(foto)

                // La copia definitiva ya se guardó y registró en Room.
                withContext(Dispatchers.IO) {
                    archivoTemporal?.delete()
                }

                _uiState.update {
                    it.copy(
                        descripcion = "",
                        guardando = false,
                        mensaje = "Fotografía guardada correctamente."
                    )
                }
            } catch (_: Exception) {
                // Si falló el registro, elimina el archivo para no dejar
                // imágenes huérfanas en el almacenamiento privado.
                withContext(Dispatchers.IO) {
                    archivoDestino?.delete()
                    archivoTemporal?.delete()
                }

                _uiState.update {
                    it.copy(
                        guardando = false,
                        error = "No fue posible guardar la fotografía."
                    )
                }
            }
        }
    }

    /**
     * Elimina el registro y el archivo privado correspondiente.
     */
    fun eliminarFotografia(fotoId: Long) {
        viewModelScope.launch {
            try {
                val foto = fotoRepository.obtenerPorId(fotoId)

                if (foto == null) {
                    mostrarError("La fotografía ya no existe.")
                    return@launch
                }

                // Valida que la ruta corresponda a nuestra carpeta privada.
                val carpetaFotos = File(
                    appContext.filesDir,
                    "photos/trabajos"
                ).canonicalFile

                val archivo = File(
                    appContext.filesDir,
                    foto.rutaArchivo
                ).canonicalFile

                if (!archivo.toPath().startsWith(carpetaFotos.toPath())) {
                    mostrarError("La ruta de la fotografía no es válida.")
                    return@launch
                }

                // Primero elimina el registro. Si falla el archivo, se
                // conserva el registro de base de datos para recuperación.
                fotoRepository.eliminarPorId(fotoId)

                withContext(Dispatchers.IO) {
                    archivo.delete()
                }

                _uiState.update {
                    it.copy(mensaje = "Fotografía eliminada.")
                }
            } catch (_: Exception) {
                mostrarError("No fue posible eliminar la fotografía.")
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update {
            it.copy(error = null, mensaje = null)
        }
    }

    private fun mostrarError(mensaje: String) {
        _uiState.update {
            it.copy(error = mensaje, mensaje = null)
        }
    }
}