package com.example.android_app.data.remote

import com.example.android_app.data.remote.api.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // TODO: Cambiar a la IP real del servidor antes de publicar
    // Para emulador Android: 10.0.2.2 = localhost del PC
    private const val BASE_URL = "http://192.168.1.66:3000/api/"

    private var authToken: String = ""

    fun setToken(token: String) { authToken = token }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val req = chain.request().newBuilder().apply {
            if (authToken.isNotEmpty()) addHeader("Authorization", "Bearer $authToken")
        }.build()
        chain.proceed(req)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}
