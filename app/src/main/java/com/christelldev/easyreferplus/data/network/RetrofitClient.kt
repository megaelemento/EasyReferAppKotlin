package com.christelldev.easyreferplus.data.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // No usar const val - obtener URL dinámicamente de AppConfig
    private const val TIMEOUT_SECONDS = 30L

    @Volatile
    private var authRepository: AuthRepository? = null

    @Volatile
    private var context: Context? = null

    @Volatile
    private var okHttpClient: OkHttpClient? = null

    @Volatile
    private var retrofit: Retrofit? = null

    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                android.util.Log.d("OkHttp", message)
            }
        }).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private fun buildOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(createLoggingInterceptor())
            //.addInterceptor(ServerUnavailableInterceptor()) // Desactivado temporalmente

        // Agregar el interceptor de autenticación si está disponible
        val repo = authRepository
        val ctx = context
        if (repo != null && ctx != null) {
            builder.addInterceptor(AuthInterceptor(repo, ctx))
        }

        return builder
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Synchronized
    fun getInstance(): Retrofit {
        if (retrofit == null) {
            if (okHttpClient == null) {
                okHttpClient = buildOkHttpClient()
            }
            // Usar URL dinámica de AppConfig
            retrofit = Retrofit.Builder()
                .baseUrl(AppConfig.BASE_URL)
                .client(okHttpClient!!)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    @Synchronized
    fun setAuthRepository(repository: AuthRepository, appContext: Context) {
        authRepository = repository
        context = appContext
        // Recrear el cliente HTTP con el nuevo repositorio
        okHttpClient = buildOkHttpClient()
        // Recrear Retrofit con la URL dinámica
        retrofit = Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .client(okHttpClient!!)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}
