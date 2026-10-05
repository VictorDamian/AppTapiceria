
package com.tapiceria.app.ui.atenciones

import com.tapiceria.app.data.local.entity.ClienteEntity
import com.tapiceria.app.domain.model.AtencionListado

/**
 * Estado de la pantalla de atenciones.
 *
 * Mantiene separados los datos del formulario, los catálogos
 * y los mensajes que se muestran al usuario.
 */
data class AtencionUiState(
    /**
     * Lista de atenciones registradas.
     */
    val atenciones: List<AtencionListado> = emptyList(),

    /**
     * Clientes disponibles para seleccionar.
     *
     * Se conservan también los inactivos para poder editar
     * correctamente una atención histórica.
     */
    val clientes: List<ClienteEntity> = emptyList(),

    /**
     * Cliente seleccionado.
     *
     * Puede ser null porque una atención puede quedar
     * sin cliente.
     */
    val clienteSeleccionadoId: Long? = null,

    /**
     * Texto utilizado para buscar clientes.
     */
    val textoBusquedaCliente: String = "",

    /**
     * ID de la atención que estamos editando.
     *
     * Null significa que estamos creando una nueva.
     */
    val atencionEditandoId: Long? = null,

    val tipoSeleccionado: String = "CONSULTA",

    val descripcion: String = "",

    val notas: String = "",

    val cargando: Boolean = true,

    val guardando: Boolean = false,

    val error: String? = null,

    val mensaje: String? = null
)