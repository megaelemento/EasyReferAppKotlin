package com.christelldev.easyreferplus.util

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import android.util.Log

/**
 * Controla el estado de bloqueo de la app.
 *
 * La pantalla de bloqueo se muestra cuando:
 * 1. El app arranca con sesión existente (primer acceso post-login).
 * 2. La app estuvo en background por más de [LOCK_TIMEOUT_MS] ms.
 *
 * El estado de pago pendiente se persiste en SharedPreferences para
 * sobrevivir a recreación de la Activity al retornar de PayPal.
 */
object AppLockManager {

    private const val TAG = "AppLockManager"

    /** Tiempo en background antes de requerir reautenticación: 30 segundos */
    private const val LOCK_TIMEOUT_MS = 30_000L

    /** Tiempo máximo que puede durar un pago pendiente antes de expirar: 10 minutos */
    private const val PAYMENT_EXPIRY_MS = 600_000L

    private const val PREFS_NAME = "app_lock_prefs"
    private const val KEY_PAYMENT_IN_PROGRESS = "payment_in_progress"
    private const val KEY_PENDING_ORDER_ID = "pending_order_id"
    private const val KEY_PENDING_PAYPAL_ID = "pending_paypal_id"
    private const val KEY_PENDING_NEEDS_DELIVERY = "pending_needs_delivery"
    private const val KEY_PENDING_COMPANY_ID = "pending_company_id"
    private const val KEY_PENDING_AMOUNT = "pending_amount"
    private const val KEY_PENDING_TIMESTAMP = "pending_timestamp"
    private const val KEY_RETURNED_FROM_PAYPAL = "returned_from_paypal"

    /** Indica si el lock screen debe mostrarse en el próximo foreground */
    private var locked = false

    /** Timestamp (SystemClock.elapsedRealtime) del último momento en background */
    private var backgroundedAt: Long = 0L

    private var prefs: SharedPreferences? = null
    private var inMemoryPaymentInProgress = false
    private var inMemoryPendingPayment: PendingPayment? = null

    /** Datos del pago pendiente para recuperación tras unlock */
    data class PendingPayment(
        val orderId: Int,
        val paypalOrderId: String,
        val needsDelivery: Boolean,
        val companyId: Int?,
        val amount: Double,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Debe llamarse desde Application.onCreate() para tener acceso a SharedPreferences.
     */
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            // Recuperar estado de pago pendiente que sobrevivió a recreación de Activity
            loadFromPrefs()
        }
    }

    private fun loadFromPrefs() {
        val p = prefs ?: return
        val inProgress = p.getBoolean(KEY_PAYMENT_IN_PROGRESS, false)
        if (inProgress) {
            val orderId = p.getInt(KEY_PENDING_ORDER_ID, -1)
            val paypalId = p.getString(KEY_PENDING_PAYPAL_ID, null)
            val timestamp = p.getLong(KEY_PENDING_TIMESTAMP, 0L)

            // Verificar expiración
            if (orderId >= 0 && paypalId != null && System.currentTimeMillis() - timestamp < PAYMENT_EXPIRY_MS) {
                inMemoryPaymentInProgress = true
                inMemoryPendingPayment = PendingPayment(
                    orderId = orderId,
                    paypalOrderId = paypalId,
                    needsDelivery = p.getBoolean(KEY_PENDING_NEEDS_DELIVERY, false),
                    companyId = p.getInt(KEY_PENDING_COMPANY_ID, -1).takeIf { it >= 0 },
                    amount = p.getFloat(KEY_PENDING_AMOUNT, 0f).toDouble(),
                    timestamp = timestamp
                )
                Log.d(TAG, "Restored pending payment from prefs: orderId=$orderId paypalId=$paypalId")
            } else {
                // Expirado, limpiar
                clearPaymentFromPrefs()
                Log.d(TAG, "Pending payment expired, cleared from prefs")
            }
        }
    }

    private fun savePaymentToPrefs(pending: PendingPayment) {
        prefs?.edit()?.apply {
            putBoolean(KEY_PAYMENT_IN_PROGRESS, true)
            putInt(KEY_PENDING_ORDER_ID, pending.orderId)
            putString(KEY_PENDING_PAYPAL_ID, pending.paypalOrderId)
            putBoolean(KEY_PENDING_NEEDS_DELIVERY, pending.needsDelivery)
            pending.companyId?.let { putInt(KEY_PENDING_COMPANY_ID, it) }
                ?: remove(KEY_PENDING_COMPANY_ID)
            putFloat(KEY_PENDING_AMOUNT, pending.amount.toFloat())
            putLong(KEY_PENDING_TIMESTAMP, pending.timestamp)
            commit() // commit síncrono para garantizar persistencia inmediata
        }
        Log.d(TAG, "Saved payment to prefs: orderId=${pending.orderId} paypalId=${pending.paypalOrderId}")
    }

    private fun clearPaymentFromPrefs() {
        prefs?.edit()?.apply {
            remove(KEY_PAYMENT_IN_PROGRESS)
            remove(KEY_PENDING_ORDER_ID)
            remove(KEY_PENDING_PAYPAL_ID)
            remove(KEY_PENDING_NEEDS_DELIVERY)
            remove(KEY_PENDING_COMPANY_ID)
            remove(KEY_PENDING_AMOUNT)
            remove(KEY_PENDING_TIMESTAMP)
            commit()
        }
        Log.d(TAG, "Cleared payment from prefs")
    }

    // ─── API pública ────────────────────────────────────────────────────────

    /** Llama esto cuando el usuario inicia sesión por primera vez */
    fun lockOnLogin() {
        locked = true
    }

    /** Llama esto cuando la app pasa a background */
    fun onAppBackgrounded() {
        backgroundedAt = SystemClock.elapsedRealtime()
    }

    /**
     * Llama esto cuando la app vuelve a foreground.
     * @return true si debe mostrarse el lock screen
     */
    fun onAppForegrounded(): Boolean {
        // Recargar de prefs por si la Activity fue recreada
        if (!inMemoryPaymentInProgress) loadFromPrefs()

        // No bloquear si hay un pago PayPal en curso
        if (inMemoryPaymentInProgress) {
            Log.d(TAG, "onAppForegrounded: payment in progress, skipping lock")
            return false
        }
        if (locked) return true
        if (backgroundedAt == 0L) return false
        val elapsed = SystemClock.elapsedRealtime() - backgroundedAt
        if (elapsed >= LOCK_TIMEOUT_MS) {
            locked = true
            return true
        }
        return false
    }

    /**
     * Verifica si hay un pago pendiente que debería suprimir el lock inicial
     * (cuando la Activity fue recreada y startDestination = AppLock).
     * @return true si se debe saltar el lock screen inicial
     */
    fun shouldSkipInitialLock(): Boolean {
        loadFromPrefs()
        return inMemoryPaymentInProgress
    }

    /** Llama esto cuando el usuario se autentica exitosamente */
    fun unlock() {
        locked = false
        backgroundedAt = 0L
    }

    /** Llama esto cuando el usuario cierra sesión completamente */
    fun reset() {
        locked = false
        backgroundedAt = 0L
        clearPaymentInProgress()
    }

    val isLocked: Boolean get() = locked

    // ─── Control de pago en curso ──────────────────────────────────────────

    /** Marca que hay un pago PayPal en curso. Suprime el lock screen. */
    fun startPayment(pending: PendingPayment) {
        inMemoryPaymentInProgress = true
        inMemoryPendingPayment = pending
        savePaymentToPrefs(pending)
        Log.d(TAG, "startPayment: payment in progress, lock suppressed. orderId=${pending.orderId}")
    }

    /** Obtiene el pago pendiente si aún no ha expirado */
    fun getPendingPayment(): PendingPayment? {
        val p = inMemoryPendingPayment
        if (p != null && System.currentTimeMillis() - p.timestamp > PAYMENT_EXPIRY_MS) {
            clearPaymentInProgress()
            return null
        }
        // Si no hay en memoria, intentar cargar de prefs
        if (p == null) {
            loadFromPrefs()
            return inMemoryPendingPayment
        }
        return p
    }

    // ─── Detección de retorno de PayPal ────────────────────────────────────

    /**
     * Marca que el usuario acaba de retornar del navegador de PayPal.
     * Se llama desde onNewIntent cuando llega el deep link.
     */
    fun markReturnedFromPayPal() {
        prefs?.edit()?.putBoolean(KEY_RETURNED_FROM_PAYPAL, true)?.commit()
        Log.d(TAG, "markReturnedFromPayPal: flagged return from PayPal browser")
    }

    /**
     * Consume el flag de retorno de PayPal. Retorna true si el usuario
     * acaba de volver del navegador (y hay que disparar la captura).
     */
    fun consumeReturnFromPayPal(): Boolean {
        val p = prefs ?: return false
        val returned = p.getBoolean(KEY_RETURNED_FROM_PAYPAL, false)
        if (returned) {
            p.edit()?.remove(KEY_RETURNED_FROM_PAYPAL)?.commit()
            Log.d(TAG, "consumeReturnFromPayPal: consumed flag, returned=$returned")
        }
        return returned
    }

    /**
     * Retorna el PayPal order ID guardado para poder llamar al capture manualmente.
     */
    fun getPendingPaypalOrderId(): String? {
        return inMemoryPendingPayment?.paypalOrderId
            ?: prefs?.getString(KEY_PENDING_PAYPAL_ID, null)
    }

    /** Limpia el estado de pago en curso */
    fun clearPaymentInProgress() {
        inMemoryPaymentInProgress = false
        inMemoryPendingPayment = null
        clearPaymentFromPrefs()
        Log.d(TAG, "clearPaymentInProgress: payment state cleared")
    }
}
