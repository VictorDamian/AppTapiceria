package com.tapiceria.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Guarda la referencia a una fotografía, no sus bytes.
 */
@Entity(
    tableName = "fotos_trabajo",
    foreignKeys = [
        ForeignKey(
            entity = TrabajoEntity::class,
            parentColumns = ["id"],
            childColumns = ["trabajoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trabajoId")]
)
data class FotoTrabajoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val trabajoId: Long,

    // Valores previstos: ANTES y DESPUES.
    val tipo: String,

    // Ruta relativa dentro del almacenamiento privado de la app.
    val rutaArchivo: String,

    val descripcion: String = "",
    val fechaRegistro: Long = System.currentTimeMillis(),

    // Rotación aplicada a la imagen, en grados.
    val rotacionGrados: Int = 0
)