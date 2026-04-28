package com.christelldev.easyreferplus

import android.app.Application
import com.christelldev.easyreferplus.util.AppLockManager
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.config.CheckoutConfig
import com.paypal.checkout.config.Environment
import com.paypal.checkout.config.SettingsConfig
import com.paypal.checkout.createorder.CurrencyCode
import com.paypal.checkout.createorder.UserAction

class EasyReferApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicializar AppLockManager con contexto para persistencia
        AppLockManager.init(this)

        // Configuración de PayPal
        // NOTA: Para producción cambiar a Environment.LIVE
        val config = CheckoutConfig(
            application = this,
            clientId = BuildConfig.PAYPAL_CLIENT_ID,
            environment = Environment.SANDBOX,
            returnUrl = "${BuildConfig.APPLICATION_ID}://paypalpay",
            currencyCode = CurrencyCode.USD,
            userAction = UserAction.PAY_NOW,
            settingsConfig = SettingsConfig(
                loggingEnabled = true
            )
        )
        PayPalCheckout.setConfig(config)
    }
}
