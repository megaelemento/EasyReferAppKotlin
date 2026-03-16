package com.christelldev.easyreferplus.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.SecureRandom

object BiometricHelper {

    private const val PREFS_NAME = "wallet_secure_prefs"
    private const val KEY_PIN = "wallet_pin"

    private fun getEncryptedPrefs(context: Context) = EncryptedSharedPreferences.create(
        PREFS_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun isPinStored(context: Context): Boolean =
        getEncryptedPrefs(context).contains(KEY_PIN)

    fun getStoredPin(context: Context): String? =
        getEncryptedPrefs(context).getString(KEY_PIN, null)

    fun storePin(context: Context, pin: String) {
        getEncryptedPrefs(context).edit().putString(KEY_PIN, pin).apply()
    }

    fun generatePin(): String =
        String.format("%06d", SecureRandom().nextInt(1_000_000))

    fun canAuthenticate(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        // Primero verificar biometría, luego credencial del dispositivo como fallback
        return manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS ||
        manager.canAuthenticate(
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun hasBiometrics(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Muestra el prompt biométrico para confirmar una TRANSFERENCIA de billetera.
     */
    fun showPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        showBiometricPrompt(
            activity = activity,
            title = "Confirmar transferencia",
            subtitle = "Verifica tu identidad para continuar",
            onSuccess = onSuccess,
            onError = onError
        )
    }

    /**
     * Muestra el prompt biométrico para acceder a la APP (AppLockScreen).
     * Usa huella dactilar, Face ID, PIN, patrón o contraseña del dispositivo
     * según lo que el usuario tenga configurado en su teléfono.
     */
    fun showAppLockPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val allowedAuthenticators = if (hasBiometrics(activity)) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }

        val prompt = BiometricPrompt(
            activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> onCancelled()
                        else -> onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    // Intento fallido — el sistema ya muestra feedback visual, no hacer nada
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceder a EasyRefer")
            .setSubtitle("Usa tu huella, Face ID o PIN del teléfono")
            .setAllowedAuthenticators(allowedAuthenticators)
            .build()

        prompt.authenticate(promptInfo)
    }

    // ─── Helper interno compartido ───────────────────────────────────────────

    private fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val allowedAuthenticators = if (hasBiometrics(activity)) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }

        val prompt = BiometricPrompt(
            activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        onError(errString.toString())
                    }
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(allowedAuthenticators)
            .build()

        prompt.authenticate(promptInfo)
    }
}
