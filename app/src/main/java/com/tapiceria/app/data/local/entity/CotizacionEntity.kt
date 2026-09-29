package com.tapiceria.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Almacena el importe y el estado de una cotización.
 */
@Entity(
    tableName = "cotizaciones",
    foreignKeys = [
        ForeignKey(
            entity = AtencionEntity::class,
            parentColumns = ["id"],
            childColumns = ["atencionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("atencionId")]
)
data class CotizacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val atencionId: Long,
    val folio: String,
    val descripcion: String,

    // Usar centavos evita errores de precisión de tipo Float.
    val importeCentavos: Long,

    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaVigencia: Long? = null,

    // Valores previstos: PENDIENTE, ACEPTADA, RECHAZADA y VENCIDA.
    val estado: String = "PENDIENTE"
)