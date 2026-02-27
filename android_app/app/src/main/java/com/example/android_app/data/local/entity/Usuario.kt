package com.example.android_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
    val nombre: String = "",
    val email: String = "",
    val rol: String = "TOMADOR",
    val token: String = ""
)
