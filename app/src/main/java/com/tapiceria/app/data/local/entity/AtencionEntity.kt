package com.tapiceria.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Registra una visita o solicitud de un cliente.
 * Una atención puede ser una consulta o una solicitud de cotización.
 */
@Entity(
    tableName = "atenciones",
    foreignKeys = [
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("clienteId")]
)
data class AtencionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val clienteId: Long,

    // Valores previstos: CONSULTA y COTIZACION.
    val tipo: String,

    val descripcion: String,
    val fechaAtencion: Long = System.currentTimeMillis(),
    val notas: String = ""
)