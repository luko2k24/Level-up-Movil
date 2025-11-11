package com.example.level_up.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


private const val BASE_IP = "http://10.0.2.2"

object RetrofitClient {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Muestra toda la petición en Logcat
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // Función base para conectar a cualquier puerto de tu backend
    fun getClient(port: Int): Retrofit {
        val baseUrl = "$BASE_IP:$port/api/" // Ej: http://10.0.2.2:8085/api/
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}

// Acceso singleton a todos los servicios de la API
object ApiClient {
    // Puertos de los microservicios:
    val usuarioService: UsuarioService by lazy { RetrofitClient.getClient(8085).create(UsuarioService::class.java) } // Puerto 8085
    val productoService: ProductoService by lazy { RetrofitClient.getClient(8081).create(ProductoService::class.java) } // Puerto 8081
    val carritoService: CarritoService by lazy { RetrofitClient.getClient(8083).create(CarritoService::class.java) } // Puerto 8083
    val pedidoService: PedidoService by lazy { RetrofitClient.getClient(8083).create(PedidoService::class.java) } // Puerto 8083
    val reseniaService: ReseniaService by lazy { RetrofitClient.getClient(8084).create(ReseniaService::class.java) } // Puerto 8084
}