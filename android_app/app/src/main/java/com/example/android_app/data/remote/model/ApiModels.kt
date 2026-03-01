package com.example.android_app.data.remote.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("usuario") val usuario: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("usuario") val usuario: UsuarioResponse
)

data class UsuarioResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("rol") val rol: String
)

data class HojaTiempoResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("numeroHoja") val numeroHoja: String,
    @SerializedName("loteId") val loteId: Int?,
    @SerializedName("tomadorId") val tomadorId: Int,
    @SerializedName("fechaEmision") val fechaEmision: String,
    @SerializedName("turno") val turno: String,
    @SerializedName("estado") val estado: String
)

data class RegistroTiempoRequest(
    @SerializedName("hojaId") val hojaId: Int,
    @SerializedName("empleadoId") val empleadoId: Int,
    @SerializedName("actividad") val actividad: String,
    @SerializedName("horaEntrada") val horaEntrada: String
)

data class RegistroSalidaRequest(
    @SerializedName("horaSalida") val horaSalida: String
)

data class RegistroTiempoResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("hojaId") val hojaId: Int,
    @SerializedName("empleadoId") val empleadoId: Int,
    @SerializedName("horaEntrada") val horaEntrada: String,
    @SerializedName("horaSalida") val horaSalida: String?,
    @SerializedName("horasTotales") val horasTotales: Double?,
    @SerializedName("estado") val estado: String
)

// ── Registro de nuevo usuario ──────────────────────────────
data class RegisterRequest(
    @SerializedName("usuario") val usuario: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("password") val password: String
)

data class RegisterResponse(
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("usuario") val usuario: UsuarioResponse
)
