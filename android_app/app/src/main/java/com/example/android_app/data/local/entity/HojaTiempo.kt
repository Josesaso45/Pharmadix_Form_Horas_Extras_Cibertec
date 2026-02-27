package com.example.android_app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hojas_tiempo",
    foreignKeys = [ForeignKey(
        entity = Lote::class,
        parentColumns = ["id"],
        childColumns = ["loteId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("loteId")]
)
data class HojaTiempo(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
    val numeroHoja: String = "",
    val loteId: Int? = null,
    val tomadorId: Int = 0,
    val fechaEmision: String = "",
    val turno: String = "DIA",
    val estado: String = "BORRADOR",
    val sincronizada: Boolean = false
)
