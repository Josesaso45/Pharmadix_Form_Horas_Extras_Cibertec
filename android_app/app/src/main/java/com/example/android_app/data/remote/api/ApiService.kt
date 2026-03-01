package com.example.android_app.data.remote.api

import com.example.android_app.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("hojas")
    suspend fun obtenerHojas(@Query("tomadorId") tomadorId: Int): Response<List<HojaTiempoResponse>>

    @GET("hojas/{id}")
    suspend fun obtenerHoja(@Path("id") id: Int): Response<HojaTiempoResponse>

    @POST("registros")
    suspend fun registrarEntrada(@Body request: RegistroTiempoRequest): Response<RegistroTiempoResponse>

    @PUT("registros/{id}/salida")
    suspend fun registrarSalida(@Path("id") registroId: Int, @Body request: RegistroSalidaRequest): Response<RegistroTiempoResponse>
}
