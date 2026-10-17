package com.example.apis3.perros

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface PerroService {
    @GET("breed/{raza}/images/random")
    suspend fun obtenerPerro(@Path("raza") raza: String): PerroDto
}

object PerroApi {
    val service: PerroService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://dog.ceo/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PerroService::class.java)
    }
}