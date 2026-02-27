package com.example.android_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "empleados")
data class Empleado(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
    val gafete: String = "",
    val nombre: String = "",
    val puesto: String = "",
    val foto: String? = null,
    val activo: Boolean = true
)
