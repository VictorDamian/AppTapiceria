package com.tapiceria.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Catálogo de servicios ofrecidos por la tapicería.
 */
@Entity(tableName = "servicios")
data class ServicioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val nombre: String,
    val descripcion: String = "",
    val activo: Boolean = true
)