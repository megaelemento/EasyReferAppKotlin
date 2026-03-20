package com.christelldev.easyreferplus.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christelldev.easyreferplus.data.model.CommissionDetail
import com.christelldev.easyreferplus.data.model.TransactionReceipt
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: String,
    viewModel: com.christelldev.easyreferplus.ui.viewmodel.TransactionDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(transactionId) {
        viewModel.loadTransactionDetail(transactionId)
    }

    Scaffold(
        containerColor = if (isDark) DesignConstants.BackgroundDark else DesignConstants.BackgroundLight
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DesignConstants.PrimaryColor)
                }
            } else if (uiState.errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    ErrorContent(
                        message = uiState.errorMessage!!,
                        isDark = isDark,
                        onRetry = { viewModel.loadTransactionDetail(transactionId) }
                    )
                }
            } else {
                uiState.receipt?.let { receipt ->
                    TransactionDetailContent(
                        receipt = receipt,
                        isDark = isDark,
                        onBack = onBack
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionDetailContent(
    receipt: TransactionReceipt,
    isDark: Boolean,
    onBack: () -> Unit
) {
    val isSale = receipt.type == "sale"
    val scrollState = rememberScrollState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo de Header con Gradiente Dinámico
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            if (isSale) listOf(DesignConstants.SuccessColor.copy(alpha = 0.4f), Color.Transparent)
                            else listOf(DesignConstants.PrimaryDark.copy(alpha = 0.6f), Color.Transparent)
                        } else {
                            if (isSale) DesignConstants.GradientSuccess else DesignConstants.GradientPrimary
                        }
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Espacio para TopBar Flotante
            Spacer(modifier = Modifier.height(60.dp))

            // Monto Principal (Header Flotante)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    color = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSale) Icons.Default.Store else Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = if (isSale) DesignConstants.SuccessColor else DesignConstants.PrimaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isSale) "Venta Realizada" else "Detalle de Compra",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "${receipt.currency} ${String.format(Locale.US, "%.2f", receipt.amount)}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Contenido en Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignConstants.CARD_MARGIN_HORIZONTAL),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info de Empresa
                ElegantDetailCard(title = "Comercio", isDark = isDark) {
                    DetailRow("Nombre", receipt.companyName ?: "N/A", isDark)
                    receipt.companyAddress?.let { DetailRow("Dirección", it, isDark) }
                    receipt.companyPhone?.let { DetailRow("Teléfono", it, isDark) }
                }

                // Info de Transacción
                ElegantDetailCard(title = "Transacción", isDark = isDark) {
                    DetailRow("ID", maskTransactionId(receipt.transactionId), isDark)
                    DetailRow("Fecha", formatDate(receipt.transactionDate), isDark)
                    receipt.referralCodeUsed?.let { DetailRow("Cupón Referido", it, isDark) }
                    receipt.description?.let { DetailRow("Concepto", it, isDark) }
                }

                // Info de Comprador (Ventas)
                if (isSale) {
                    ElegantDetailCard(title = "Datos del Cliente", isDark = isDark) {
                        DetailRow("Nombre", receipt.buyerName ?: "N/A", isDark)
                        receipt.buyerPhone?.let { DetailRow("Teléfono", it, isDark) }
                    }
                }

                // Desglose de Comisiones
                receipt.commissionInfo?.let { commissionInfo ->
                    ElegantCommissionCard(
                        commissionInfo = commissionInfo,
                        currency = receipt.currency,
                        isDark = isDark
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Top Bar Flotante (Glass Style)
        ElegantTopBar(onBack = onBack)
    }
}

@Composable
private fun ElegantTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.3f),
            onClick = onBack
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        
        Text(
            text = "Recibo Digital",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Box(modifier = Modifier.size(40.dp)) // Balance
    }
}

@Composable
private fun ElegantDetailCard(
    title: String,
    isDark: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
        color = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = if (isDark) DesignConstants.TextPrimaryDark else DesignConstants.TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (isDark) DesignConstants.TextPrimaryDark else DesignConstants.TextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 16.dp)
        )
    }
}

@Composable
private fun ElegantCommissionCard(
    commissionInfo: com.christelldev.easyreferplus.data.model.CommissionInfo,
    currency: String,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
        color = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
        tonalElevation = 2.dp,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = DesignConstants.SuccessColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Percent, null, tint = DesignConstants.SuccessColor, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Desglose de Comisiones",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) DesignConstants.TextPrimaryDark else DesignConstants.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DesignConstants.PrimaryColor.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Repartido",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary
                    )
                    Text(
                        text = "$currency ${String.format(Locale.US, "%.2f", commissionInfo.totalCommissionAmount ?: 0.0)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = DesignConstants.SuccessColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val details = commissionInfo.commissionDetails
            if (!details.isNullOrEmpty()) {
                details.forEachIndexed { index, detail ->
                    ElegantLevelItem(level = index + 1, detail = detail, currency = currency, isDark = isDark)
                    if (index < details.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
                        )
                    }
                }
            } else {
                Text(
                    text = "Esta transacción no generó comisiones de red.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DesignConstants.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun ElegantLevelItem(
    level: Int,
    detail: CommissionDetail,
    currency: String,
    isDark: Boolean
) {
    val levelColor = when (level) {
        1 -> Color(0xFF10B981)
        2 -> Color(0xFF03A9F4)
        else -> Color(0xFF8B5CF6)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = levelColor.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "$level", color = levelColor, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall)
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Nivel $level",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) DesignConstants.TextPrimaryDark else DesignConstants.TextPrimary
            )
            Text(
                text = detail.userReferralCode ?: "Directo",
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$currency ${String.format(Locale.US, "%.2f", detail.netCommission ?: 0.0)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = levelColor
            )
            Text(
                text = "${detail.percentageOfDistribution ?: 0.0}% dist.",
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) DesignConstants.TextSecondaryDark.copy(alpha = 0.6f) else DesignConstants.TextSecondary.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ErrorContent(message: String, isDark: Boolean, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(64.dp), tint = DesignConstants.ErrorColor)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = DesignConstants.PrimaryColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Reintentar")
        }
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString == null) return "N/A"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("es-EC"))
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

private fun maskTransactionId(transactionId: String): String {
    return if (transactionId.length > 8) "****${transactionId.takeLast(8)}" else transactionId
}
