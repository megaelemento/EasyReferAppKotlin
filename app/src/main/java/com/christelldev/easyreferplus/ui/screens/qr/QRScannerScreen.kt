package com.christelldev.easyreferplus.ui.screens.qr

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.ui.theme.AppBlue
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun QRScannerScreen(
    onQRCodeScanned: (code: String, secret: String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            showRationale = true
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onSurface)
    ) {
        when {
            hasCameraPermission -> {
                CameraPreviewContent(
                    onQRCodeScanned = onQRCodeScanned,
                    onClose = onClose
                )
            }
            showRationale -> {
                PermissionRationaleContent(
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onClose = onClose
                )
            }
            else -> {
                PermissionDeniedContent(onClose = onClose)
            }
        }
    }
}

@androidx.camera.core.ExperimentalGetImage
@Composable
private fun CameraPreviewContent(
    onQRCodeScanned: (code: String, secret: String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var flashEnabled by remember { mutableStateOf(false) }
    var scannedOnce by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also { it.surfaceProvider = previewView.surfaceProvider }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(
                                Executors.newSingleThreadExecutor()
                            ) { imageProxy ->
                                if (!scannedOnce) {
                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null) {
                                        val image = InputImage.fromMediaImage(
                                            mediaImage,
                                            imageProxy.imageInfo.rotationDegrees
                                        )
                                        val scanner = BarcodeScanning.getClient()
                                        scanner.process(image)
                                            .addOnSuccessListener { barcodes ->
                                                for (barcode in barcodes) {
                                                    if (barcode.format == Barcode.FORMAT_QR_CODE) {
                                                        val rawValue = barcode.rawValue ?: ""
                                                        val parsed = parseQRData(rawValue)
                                                        if (parsed != null) {
                                                            scannedOnce = true
                                                            onQRCodeScanned(parsed.first, parsed.second)
                                                        }
                                                    }
                                                }
                                            }
                                            .addOnCompleteListener {
                                                imageProxy.close()
                                            }
                                    } else {
                                        imageProxy.close()
                                    }
                                } else {
                                    imageProxy.close()
                                }
                            }
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // Overlay with scanning frame
        QRScannerOverlay()

        // Top bar with close and flash buttons
        QRScannerTopBar(
            flashEnabled = flashEnabled,
            onFlashToggle = {
                flashEnabled = !flashEnabled
                camera?.cameraControl?.enableTorch(flashEnabled)
            },
            onClose = onClose
        )

        // Bottom instruction
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            QRScannerBottomInstruction()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            camera?.cameraControl?.enableTorch(false)
        }
    }
}

private fun parseQRData(rawValue: String): Pair<String, String>? {
    return try {
        if (rawValue.startsWith("easyrefer://pay?")) {
            val params = rawValue.removePrefix("easyrefer://pay?")
            val codeParam = params.split("&").find { it.startsWith("code=") }
            val secretParam = params.split("&").find { it.startsWith("secret=") }

            val code = codeParam?.removePrefix("code=") ?: ""
            val secret = secretParam?.removePrefix("secret=") ?: ""

            if (code.isNotEmpty() && secret.isNotEmpty()) {
                Pair(code, secret)
            } else null
        } else {
            // Formato: QR_CODE:{qr_code}|COMPANY:{company_id}
            val parts = rawValue.split("|")
            if (parts.size >= 2) {
                // Extraer código sin prefijo QR_CODE:
                val codeWithPrefix = parts[0]
                val code = if (codeWithPrefix.startsWith("QR_CODE:")) {
                    codeWithPrefix.removePrefix("QR_CODE:")
                } else {
                    codeWithPrefix
                }
                // Para QR_CODE formato, no hay secreto real - el backend lo obtiene de la BD
                Pair(code, "")
            } else if (parts.isNotEmpty()) {
                val codeWithPrefix = parts[0]
                val code = if (codeWithPrefix.startsWith("QR_CODE:")) {
                    codeWithPrefix.removePrefix("QR_CODE:")
                } else {
                    codeWithPrefix
                }
                Pair(code, "")
            } else null
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun QRScannerOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(Color.Transparent)
        )
    }
}

@Composable
private fun QRScannerTopBar(
    flashEnabled: Boolean,
    onFlashToggle: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), RoundedCornerShape(50))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.close),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(
            onClick = onFlashToggle,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), RoundedCornerShape(50))
        ) {
            Icon(
                imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = stringResource(R.string.toggle_flash),
                tint = if (flashEnabled) Color.Yellow else Color.White
            )
        }
    }
}

@Composable
private fun QRScannerBottomInstruction() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 80.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.point_camera_qr),
            color = MaterialTheme.colorScheme.surface,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PermissionRationaleContent(
    onRequestPermission: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        Text(
            text = stringResource(R.string.camera_permission_required),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.camera_permission_rationale),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
        ) {
            Text(stringResource(R.string.grant_permission))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        ) {
            Text(stringResource(R.string.cancel))
        }
    }
}

@Composable
private fun PermissionDeniedContent(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        Text(
            text = stringResource(R.string.camera_permission_denied),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.camera_permission_denied_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
        ) {
            Text(stringResource(R.string.close))
        }
    }
}
