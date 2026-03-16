package com.christelldev.easyreferplus.util

import android.os.SystemClock

/**
 * Controla el estado de bloqueo de la app.
 *
 * La pantalla de bloqueo se muestra cuando:
 * 1. El app arranca con sesión existente (primer acceso post-login).
 * 2. La app estuvo en background por más de [LOCK_TIMEOUT_MS] ms.
 *
 * No persiste en disco: si el proceso muere, la app vuelve a pedir auth.
 */
object AppLockManager {

    /** Tiempo en background antes de requerir reautenticación: 30 segundos */
    private const val LOCK_TIMEOUT_MS = 30_000L

    /** Indica si el lock screen debe mostrarse en el próximo foreground */
    private var locked = false

    /** Timestamp (SystemClock.elapsedRealtime) del último momento en background */
    private var backgroundedAt: Long = 0L

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
        if (locked) return true
        if (backgroundedAt == 0L) return false
        val elapsed = SystemClock.elapsedRealtime() - backgroundedAt
        if (elapsed >= LOCK_TIMEOUT_MS) {
            locked = true
            return true
        }
        return false
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
    }

    val isLocked: Boolean get() = locked
}
