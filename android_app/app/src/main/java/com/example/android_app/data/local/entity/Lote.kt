package com.example.android_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lotes")
data class Lote(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
    val numero: String = "",
    val producto: String = "",
    val presentacion: String = "",
    val proceso: String = "",
    val area: String = "",
    val cantidadOrdenada: Double = 0.0,
    val estado: String = "ABIERTO",
    val fechaInicio: String = ""
)
