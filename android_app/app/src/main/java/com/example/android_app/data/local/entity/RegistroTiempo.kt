package com.example.android_app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "registros_tiempo",
    foreignKeys = [
        ForeignKey(entity = HojaTiempo::class, parentColumns = ["id"], childColumns = ["hojaId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Empleado::class, parentColumns = ["id"], childColumns = ["empleadoId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("hojaId"), Index("empleadoId")]
)
data class RegistroTiempo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hojaId: Int = 0,
    val empleadoId: Int? = null,
    val actividad: String = "",
    val horaEntrada: String? = null,
    val horaSalida: String? = null,
    val horasTotales: Double? = null,
    val estado: String = "PENDIENTE"
)
