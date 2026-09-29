package com.tapiceria.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Registra anticipos y pagos asociados a un trabajo.
 */
@Entity(
    tableName = "pagos",
    foreignKeys = [
        ForeignKey(
            entity = TrabajoEntity::class,
            parentColumns = ["id"],
            childColumns = ["trabajoId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("trabajoId")]
)
data class PagoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val trabajoId: Long,
    val importeCentavos: Long,
    val fechaPago: Long = System.currentTimeMillis(),

    // Ejemplos: EFECTIVO, TRANSFERENCIA y TARJETA.
    val metodo: String,

    val referencia: String = "",
    val notas: String = ""
)