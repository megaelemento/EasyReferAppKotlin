package com.christelldev.easyreferplus.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.christelldev.easyreferplus.MainActivity
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.network.ApiService
import com.christelldev.easyreferplus.data.network.RetrofitClient
import com.christelldev.easyreferplus.ui.screens.driver.DriverOrderRequestActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EasyReferFirebaseService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID        = "easyrefer_orders"
        const val CHANNEL_NAME      = "Pedidos y entregas"
        const val CHANNEL_DRIVER_ID = "easyrefer_driver_alert"
        private const val TAG       = "EasyReferFCM"
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Nuevo token FCM: ${token.take(20)}…")
        sendTokenToBackend(token)
    }

    private fun sendTokenToBackend(token: String) {
        val prefs = getSharedPreferences("EasyReferPrefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("access_token", null) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = RetrofitClient.getInstance().create(ApiService::class.java)
                val response = api.updateFcmToken(
                    authorization = "Bearer $accessToken",
                    body = mapOf("token" to token),
                )
                if (response.isSuccessful) {
                    Log.d(TAG, "Token FCM registrado en backend OK")
                    prefs.edit().putString("fcm_token", token).apply()
                } else {
                    Log.w(TAG, "Error al registrar token FCM: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción registrando token FCM: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Mensaje FCM recibido: ${message.data}")

        // Mensaje data-only de nuevo pedido para el conductor
        // Solo mostrar la alarma si el usuario actualmente logueado es un conductor
        if (message.data["type"] == "new_order") {
            if (isCurrentUserDriver()) {
                showDriverOrderAlert(message.data)
            } else {
                Log.d(TAG, "Notificación new_order ignorada: el usuario actual no es conductor")
            }
            return
        }

        // Notificación de pedido listo para recoger (solo conductores)
        if (message.data["type"] == "order_ready_for_pickup") {
            if (isCurrentUserDriver()) {
                val title = message.data["title"] ?: "¡Pedido listo para recoger!"
                val body = message.data["body"] ?: "Dirígete al establecimiento."
                showNotification(title, body, message.data)
            }
            return
        }

        val title = message.notification?.title ?: message.data["title"] ?: "EasyRefer"
        val body  = message.notification?.body  ?: message.data["body"]  ?: return

        showNotification(title, body, message.data)
    }

    private fun showDriverOrderAlert(data: Map<String, String>) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Canal con sonido de alarma
        val alarmUri = android.media.RingtoneManager.getDefaultUri(
            android.media.RingtoneManager.TYPE_ALARM
        ) ?: android.media.RingtoneManager.getDefaultUri(
            android.media.RingtoneManager.TYPE_RINGTONE
        )
        val channel = NotificationChannel(
            CHANNEL_DRIVER_ID,
            "Alertas de pedido",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Alerta cuando llega un nuevo pedido para el conductor"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            setSound(alarmUri, android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
        }
        manager.createNotificationChannel(channel)

        // FullScreenIntent → DriverOrderRequestActivity
        val fullScreenIntent = DriverOrderRequestActivity.createIntent(this, data)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            data["order_id"]?.toIntOrNull() ?: 0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val orderId     = data["order_id"] ?: "?"
        val fee         = data["delivery_fee"] ?: "0.00"
        val dropoff     = data["dropoff_address"] ?: ""

        val notification = NotificationCompat.Builder(this, CHANNEL_DRIVER_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("¡Nuevo pedido #$orderId!")
            .setContentText("Tarifa: \$$fee — $dropoff")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setSound(alarmUri)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .build()

        manager.notify(("order_${orderId}").hashCode(), notification)

        // Lanzar la Activity directamente para garantizar que aparezca
        startActivity(fullScreenIntent)
    }

    /**
     * Verifica si el usuario actualmente logueado en este dispositivo es un conductor.
     * Se guarda un flag "is_driver" en SharedPreferences cuando el usuario accede
     * al panel de conductor.
     */
    private fun isCurrentUserDriver(): Boolean {
        val prefs = getSharedPreferences("EasyReferPrefs", Context.MODE_PRIVATE)
        // Si no hay access_token, no hay sesión activa → ignorar
        if (prefs.getString("access_token", null) == null) return false
        return prefs.getBoolean("is_driver_account", false)
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal (Android 8+)
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Notificaciones de pedidos y entregas de EasyRefer"
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)

        // Intent que abre la app al tocar la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data.forEach { (k, v) -> putExtra(k, v) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
