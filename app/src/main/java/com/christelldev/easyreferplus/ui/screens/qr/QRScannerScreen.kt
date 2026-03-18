package com.christelldev.easyreferplus.ui.screens.qr

import androidx.compose.ui.geometry.Size
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun QRScannerScreen(
    onQRCodeScanned: (String, String?) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Animación para la línea de escaneo
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "line"
    )

    // AtomicBoolean para evitar múltiples escaneos desde el hilo de cámara (no Compose)
    val alreadyScanned = remember { java.util.concurrent.atomic.AtomicBoolean(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // VISTA DE CÁMARA
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val barcodeScanner = BarcodeScanning.getClient()
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !alreadyScanned.get()) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            barcodeScanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        barcode.rawValue?.let { qrData ->
                                            if (qrData.contains("easyrefer://") && alreadyScanned.compareAndSet(false, true)) {
                                                val uri = android.net.Uri.parse(qrData)
                                                val code = uri.getQueryParameter("code") ?: ""
                                                val secret = uri.getQueryParameter("secret")
                                                if (code.isNotBlank()) {
                                                    onQRCodeScanned(code, secret)
                                                } else {
                                                    alreadyScanned.set(false) // revertir si el código está vacío
                                                }
                                            }
                                        }
                                    }
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("QRScanner", "Error binding camera", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // OVERLAY VISUAL DE ESCANEO (Design Standard)
        ScannerOverlay(scanLineProgress)

        // UI ELEMENTS (Elegant Design)
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con botón de cierre (Glass Style)
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.4f),
                    onClick = onClose
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.QrCodeScanner, null, tint = DesignConstants.PrimaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Escáner", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Placeholder para equilibrio visual
                Box(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Instrucciones (Glass Card)
            Surface(
                modifier = Modifier.padding(bottom = 48.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Escanea el código QR",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Coloca el código dentro del recuadro",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ScannerOverlay(scanLineProgress: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val boxSize = width * 0.75f
        val left = (width - boxSize) / 2
        val top = (height - boxSize) / 2
        val right = left + boxSize
        val bottom = top + boxSize
        val cornerRad = 32.dp.toPx()

        // 1. Fondo Oscuro con Hueco (Hole)
        val path = Path().apply {
            addRect(Rect(0f, 0f, width, height))
            addRoundRect(RoundRect(Rect(left, top, right, bottom), CornerRadius(cornerRad)))
        }
        
        with(drawContext.canvas.nativeCanvas) {
            val checkPoint = saveLayer(null, null)
            drawRect(Color.Black.copy(alpha = 0.7f))
            drawPath(
                path = Path().apply { 
                    addRoundRect(RoundRect(Rect(left, top, right, bottom), CornerRadius(cornerRad))) 
                },
                color = Color.Transparent,
                blendMode = BlendMode.Clear
            )
            restoreToCount(checkPoint)
        }

        // 2. Esquinas del Escáner (Más modernas que un recuadro completo)
        val strokeWidth = 4.dp.toPx()
        val lineLen = 40.dp.toPx()
        val accentColor = DesignConstants.PrimaryColor

        // Top Left
        drawPath(
            path = Path().apply {
                moveTo(left, top + lineLen)
                lineTo(left, top + cornerRad)
                arcTo(Rect(left, top, left + cornerRad * 2, top + cornerRad * 2), 180f, 90f, false)
                lineTo(left + lineLen, top)
            },
            color = accentColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Top Right
        drawPath(
            path = Path().apply {
                moveTo(right - lineLen, top)
                lineTo(right - cornerRad, top)
                arcTo(Rect(right - cornerRad * 2, top, right, top + cornerRad * 2), 270f, 90f, false)
                lineTo(right, top + lineLen)
            },
            color = accentColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Bottom Left
        drawPath(
            path = Path().apply {
                moveTo(left, bottom - lineLen)
                lineTo(left, bottom - cornerRad)
                arcTo(Rect(left, bottom - cornerRad * 2, left + cornerRad * 2, bottom), 180f, -90f, false)
                lineTo(left + lineLen, bottom)
            },
            color = accentColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Bottom Right
        drawPath(
            path = Path().apply {
                moveTo(right, bottom - lineLen)
                lineTo(right, bottom - cornerRad)
                arcTo(Rect(right - cornerRad * 2, bottom - cornerRad * 2, right, bottom), 0f, 90f, false)
                lineTo(right - lineLen, bottom)
            },
            color = accentColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 3. Línea de Escaneo Láser (Más elegante con degradado)
        val lineY = top + (boxSize * scanLineProgress)
        
        // Brillo sutil debajo de la línea
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(accentColor.copy(alpha = 0f), accentColor.copy(alpha = 0.3f), accentColor.copy(alpha = 0f)),
                startY = lineY - 20.dp.toPx(),
                endY = lineY + 20.dp.toPx()
            ),
            topLeft = Offset(left + 10.dp.toPx(), lineY - 20.dp.toPx()),
            size = Size(boxSize - 20.dp.toPx(), 40.dp.toPx())
        )

        // La línea principal
        drawLine(
            color = Color.White,
            start = Offset(left + 15.dp.toPx(), lineY),
            end = Offset(right - 15.dp.toPx(), lineY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
