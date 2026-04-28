package com.christelldev.easyreferplus.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.christelldev.easyreferplus.ui.viewmodel.PayPalViewModel
import com.paypal.checkout.paymentbutton.PaymentButtonContainer
import com.paypal.checkout.shipping.OnShippingChange

private const val TAG = "PayPalButton"

/**
 * Helper para obtener la Activity desde cualquier contexto.
 */
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun PayPalCheckoutButton(
    viewModel: PayPalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Track if PayPal SDK initialization failed
    var sdkError by remember { mutableStateOf<String?>(null) }

    // SDK v1.3.0: PaymentButtonContainer manages buttons internally.
    // Usamos setup() directamente en el container (PayPalButton es deprecado).
    val paymentButtonContainer = remember {
        try {
            PaymentButtonContainer(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                setup(
                    createOrder = viewModel.onCreateOrderCallback,
                    onApprove = viewModel.onApproveCallback,
                    onShippingChange = OnShippingChange { _, actions -> actions.approve() },
                    onCancel = viewModel.onCancelCallback,
                    onError = viewModel.onErrorCallback
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing PayPal button: ${e.message}", e)
            sdkError = e.message ?: "Error al inicializar PayPal SDK"
            null
        }
    }

    if (sdkError != null) {
        // Fallback: PayPal SDK not available
        androidx.compose.material3.Button(
            onClick = { viewModel.resetState() },
            modifier = modifier.fillMaxWidth().height(52.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            enabled = false
        ) {
            androidx.compose.material3.Text(
                "PayPal no disponible",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )
        }
    } else {
        paymentButtonContainer?.let { container ->
            AndroidView(
                modifier = modifier
                    .fillMaxWidth()
                    .height(60.dp),
                factory = { container }
            )
        }
    }
}
