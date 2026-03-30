package com.christelldev.easyreferplus.data.network

import android.content.Context
import android.content.SharedPreferences

/**
 * Configuración centralizada de la API.
 *
 * ==================== CONFIGURACIÓN ====================
 * Cambia la URL aquí según el entorno:
 *
 * LOCAL/TEST:  "http://192.168.9.13:8971"
 * PRODUCCIÓN:  "http://192.168.9.32:8000"
 *
 * ==================== INSTRUCCIONES ====================
 * 1. Modifica SERVER_URL abajo con la URL deseada
 * 2. Recompila la app
 * 3. Instala en el dispositivo
 */
object AppConfig {
    // ==================== VERSIÓN DE LA APP ====================
    const val APP_VERSION = "1.0.0"
    const val APP_VERSION_CODE = 1
    
    // ==================== URL DEL SERVIDOR ====================
    // Cambia esta línea para elegir el servidor:
    // - LOCAL:    "http://192.168.9.32:8971"
    // - PRODUCCIÓN: "https://puntodeenfoque.online"
    private const val SERVER_URL = "https://puntodeenfoque.online"

    // ==================== URLs DERIVADAS ====================

    // URL base
    val BASE_URL: String
        get() = SERVER_URL

    // URL base sin trailing slash
    val BASE_URL_CLEAN: String
        get() = SERVER_URL.trimEnd('/')

    // URL para WebSocket (convierte http/https a ws/wss)
    val WS_URL: String
        get() = when {
            SERVER_URL.startsWith("https") -> SERVER_URL.replace("https://", "wss://")
            SERVER_URL.startsWith("http") -> SERVER_URL.replace("http://", "ws://")
            else -> "ws://$SERVER_URL"
        }

    // URL para health check
    val HEALTH_URL: String
        get() = "$BASE_URL_CLEAN/health"

    // URL para obtener configuración del servidor
    val CONFIG_URL: String
        get() = "$BASE_URL_CLEAN/api/config"

    // URL para verificación de servidor
    val SERVER_CHECK_URL: String
        get() = "$BASE_URL_CLEAN/health"

    // Función de inicialización (no hace nada ahora, pero se mantiene por compatibilidad)
    fun init(context: Context) {
        // Ya no es necesario obtener la URL dinámicamente
    }

    // Función para obtener la URL base actual
    fun getBaseUrl(): String = SERVER_URL
}
