package com.christelldev.easyreferplus.ui.screens.driver

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christelldev.easyreferplus.data.network.RetrofitClient
import com.christelldev.easyreferplus.data.network.ApiService
import com.christelldev.easyreferplus.ui.theme.EasyReferPlusTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DriverOrderRequestActivity : ComponentActivity() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaPlayer: MediaPlayer? = null
    private var countDownTimer: CountDownTimer? = null

    companion object {
        const val EXTRA_ORDER_ID       = "order_id"
        const val EXTRA_DELIVERY_FEE   = "delivery_fee"
        const val EXTRA_PICKUP_ADDRESS = "pickup_address"
        const val EXTRA_DROPOFF_ADDRESS= "dropoff_address"
        const val EXTRA_ITEMS_COUNT    = "items_count"
        const val EXTRA_TOTAL          = "total"
        const val EXTRA_TIMEOUT        = "timeout_seconds"

        fun createIntent(context: Context, data: Map<String, String>): Intent =
            Intent(context, DriverOrderRequestActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_ORDER_ID,        data["order_id"]?.toIntOrNull() ?: 0)
                putExtra(EXTRA_DELIVERY_FEE,    data["delivery_fee"]?.toDoubleOrNull() ?: 0.0)
                putExtra(EXTRA_PICKUP_ADDRESS,  data["pickup_address"] ?: "")
                putExtra(EXTRA_DROPOFF_ADDRESS, data["dropoff_address"] ?: "")
                putExtra(EXTRA_ITEMS_COUNT,     data["items_count"]?.toIntOrNull() ?: 0)
                putExtra(EXTRA_TOTAL,           data["total"]?.toDoubleOrNull() ?: 0.0)
                putExtra(EXTRA_TIMEOUT,         data["timeout_seconds"]?.toIntOrNull() ?: 30)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Despertar pantalla aunque esté bloqueado
        wakeScreen()

        val orderId       = intent.getIntExtra(EXTRA_ORDER_ID, 0)
        val deliveryFee   = intent.getDoubleExtra(EXTRA_DELIVERY_FEE, 0.0)
        val pickupAddr    = intent.getStringExtra(EXTRA_PICKUP_ADDRESS) ?: ""
        val dropoffAddr   = intent.getStringExtra(EXTRA_DROPOFF_ADDRESS) ?: ""
        val itemsCount    = intent.getIntExtra(EXTRA_ITEMS_COUNT, 0)
        val total         = intent.getDoubleExtra(EXTRA_TOTAL, 0.0)
        val timeoutSec    = intent.getIntExtra(EXTRA_TIMEOUT, 30)

        startAlarm()

        setContent {
            EasyReferPlusTheme {
                OrderRequestScreen(
                    orderId       = orderId,
                    deliveryFee   = deliveryFee,
                    pickupAddress = pickupAddr,
                    dropoffAddress= dropoffAddr,
                    itemsCount    = itemsCount,
                    total         = total,
                    timeoutSeconds= timeoutSec,
                    onAccept      = { acceptOrder(orderId) },
                    onReject      = { rejectOrder(orderId) },
                    onTimeout     = { finish() },
                )
            }
        }
    }

    private fun wakeScreen() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
            PowerManager.FULL_WAKE_LOCK or
            PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "EasyRefer:OrderRequest"
        )
        wakeLock?.acquire(35_000L)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun startAlarm() {
        try {
            val am = getSystemService(AUDIO_SERVICE) as AudioManager
            am.setStreamVolume(
                AudioManager.STREAM_ALARM,
                am.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                0
            )
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_ALARM)
                setDataSource(this@DriverOrderRequestActivity, alarmUri)
                isLooping = true
                prepare()
                start()
            }
        } catch (_: Exception) {
            // Fallback silencioso
        }
    }

    private fun stopAlarm() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}
    }

    private fun acceptOrder(orderId: Int) {
        stopAlarm()
        val prefs = getSharedPreferences("EasyReferPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("access_token", null)
        if (token != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val api = RetrofitClient.getInstance().create(ApiService::class.java)
                    api.acceptOrder("Bearer $token", orderId)
                } catch (_: Exception) {}
            }
        }
        finish()
    }

    private fun rejectOrder(orderId: Int) {
        stopAlarm()
        val prefs = getSharedPreferences("EasyReferPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("access_token", null)
        if (token != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val api = RetrofitClient.getInstance().create(ApiService::class.java)
                    api.rejectOrder("Bearer $token", orderId)
                } catch (_: Exception) {}
            }
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
        countDownTimer?.cancel()
        wakeLock?.release()
    }
}

@Composable
private fun OrderRequestScreen(
    orderId: Int,
    deliveryFee: Double,
    pickupAddress: String,
    dropoffAddress: String,
    itemsCount: Int,
    total: Double,
    timeoutSeconds: Int,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onTimeout: () -> Unit,
) {
    var remainingSeconds by remember { mutableIntStateOf(timeoutSeconds) }

    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            kotlinx.coroutines.delay(1_000)
            remainingSeconds--
        }
        onTimeout()
    }

    val progress = remainingSeconds.toFloat() / timeoutSeconds.toFloat()
    val timerColor by animateColorAsState(
        when {
            progress > 0.5f -> Color(0xFF22C55E)
            progress > 0.25f -> Color(0xFFF59E0B)
            else -> Color(0xFFEF4444)
        },
        label = "timerColor"
    )

    // Pulso del círculo exterior
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "pulseScale"
    )

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0F172A)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Header
            Text(
                "¡Nuevo Pedido!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            // Timer circular
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale)
                        .background(timerColor.copy(alpha = 0.2f), CircleShape)
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(90.dp),
                    color = timerColor,
                    strokeWidth = 6.dp,
                    trackColor = Color.White.copy(alpha = 0.1f),
                )
                Text(
                    "$remainingSeconds",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = timerColor
                )
            }

            // Card con datos del pedido
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // ID + tarifa
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Pedido #$orderId",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            "$${String.format("%.2f", deliveryFee)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color(0xFF22C55E)
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    // Recogida
                    if (pickupAddress.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Store, null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )
                            Column {
                                Text("Recogida", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(pickupAddress, fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }

                    // Entrega
                    if (dropoffAddress.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn, null,
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )
                            Column {
                                Text("Entrega", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(dropoffAddress, fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$itemsCount artículo(s)", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text(
                            "Total $${String.format("%.2f", total)}",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Botones
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rechazar
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("RECHAZAR", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Aceptar
                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("ACEPTAR", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
