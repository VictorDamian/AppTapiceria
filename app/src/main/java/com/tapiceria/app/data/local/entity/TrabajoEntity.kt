package com.tapiceria.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa un trabajo contratado por un cliente.
 */
@Entity(
    tableName = "trabajos",
    foreignKeys = [
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CotizacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["cotizacionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("clienteId"),
        Index("cotizacionId"),
        Index("fechaRecepcion"),
        Index("estado")
    ]
)
data class TrabajoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val clienteId: Long,

    // Puede ser nulo si el trabajo no provino de una cotización.
    val cotizacionId: Long? = null,

    val folio: String,
    val descripcion: String,
    val importeCentavos: Long,

    val fechaRecepcion: Long = System.currentTimeMillis(),
    val fechaEntregaEstimada: Long? = null,
    val fechaEntregaReal: Long? = null,

    // Valores previstos: PENDIENTE, EN_PROCESO, TERMINADO,
    // ENTREGADO y CANCELADO.
    val estado: String = "PENDIENTE",

    val notas: String = ""
)