package com.tapiceria.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa un cliente registrado en la tapicería.
 */
@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val nombre: String,
    val telefono: String = "",
    val direccion: String = "",
    val notas: String = "",

    // Se almacena como timestamp para facilitar los filtros por fecha.
    val fechaRegistro: Long = System.currentTimeMillis(),

    // Permite conservar el historial sin eliminar físicamente al cliente.
    val activo: Boolean = true
)