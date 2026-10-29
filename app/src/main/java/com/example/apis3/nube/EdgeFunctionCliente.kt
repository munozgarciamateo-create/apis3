package com.example.apis3.nube

import android.util.Log
import com.example.apis3.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Llama a Edge Functions (escritas en SQL) de SQLite Cloud:
 *   POST {functionsUrl}/{nombre}   Header: Authorization: Bearer {apiKey}   cuerpo: {"clave": "...", ...}
 * Cada valor del cuerpo se usa en el SQL con el prefijo $ ($clave, $titulo, ...).
 * La URL base y la API key salen de local.properties (ver app/build.gradle.kts).
 */
object EdgeFunctionCliente {

    private val urlBase = BuildConfig.SQLITECLOUD_FUNCTIONS_URL.trim().trimEnd('/')
    private val apiKey = BuildConfig.SQLITECLOUD_API_KEY.trim()

    /** Si falta algún dato en local.properties, la nube queda apagada y la app sigue solo con Room. */
    val configurado: Boolean = urlBase.isNotBlank() && apiKey.isNotBlank()

    private val gson = Gson()
    private val json = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    /** Devuelve el campo "data" de la respuesta (normalmente un arreglo de filas) o null. */
    suspend fun llamar(funcion: String, parametros: Map<String, Any> = emptyMap()): JsonElement? =
        withContext(Dispatchers.IO) {
            val url = "$urlBase/$funcion"
            // La apikey va en la cabecera: todo lo del cuerpo se vuelve parámetro \$nombre del SQL
            val cuerpo = gson.toJson(parametros)
            val peticion = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $apiKey")
                .post(cuerpo.toRequestBody(json))
                .build()

            // Solo en debug: en la app publicada no se registran cuerpos ni datos de la API key
            if (BuildConfig.DEBUG) Log.d("SQLiteCloud", "→ POST $urlBase/$funcion  key=${apiKey.take(4)}…(${apiKey.length})  cuerpo=$cuerpo")
            client.newCall(peticion).execute().use { respuesta ->
                val texto = respuesta.body?.string().orEmpty()
                if (BuildConfig.DEBUG) Log.d("SQLiteCloud", "← ${respuesta.code} $funcion: ${texto.take(300)}")
                if (!respuesta.isSuccessful) {
                    throw IOException("Edge Function '$funcion' respondió ${respuesta.code}: $texto")
                }
                var data: JsonElement? = gson.fromJson(texto, JsonElement::class.java)
                // Se desenvuelve { "data": ... } (por si viene anidado más de una vez)
                while (data is JsonObject && data.has("data")) data = data.get("data")
                data
            }
        }
}
