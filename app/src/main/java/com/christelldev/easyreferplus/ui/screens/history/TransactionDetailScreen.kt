package com.christelldev.easyreferplus.ui.screens.history

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.data.model.CommissionDetail
import com.christelldev.easyreferplus.data.model.TransactionReceipt
import com.christelldev.easyreferplus.ui.theme.AppBlue
import java.text.SimpleDateFormat
import java.util.*

// Constants for consistent styling - Following HomeScreen design
private val CARD_CORNER_RADIUS = 20.dp
private val CARD_ELEVATION = 8.dp

// ====== AUXILIARY FUNCTIONS ======

@Composable
private fun DetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    // Following HomeScreen design
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
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
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: return dateString)
    } catch (e: Exception) {
        dateString
    }
}

private fun maskTransactionId(transactionId: String): String {
    // Mostrar solo los últimos 5 dígitos por seguridad
    return if (transactionId.length > 5) {
        "****${transactionId.takeLast(5)}"
    } else {
        transactionId
    }
}

private fun getStatusText(status: String): String {
    return when (status) {
        "paid" -> "Pagado"
        "pending" -> "Pendiente"
        "scheduled" -> "Programado"
        "on_hold" -> "En espera"
        "cancelled" -> "Cancelado"
        else -> status
    }
}

@Composable
private fun CommissionLevelCard(
    level: Int,
    detail: CommissionDetail,
    currency: String
) {
    val levelColors = listOf(
        Color(0xFF4CAF50), // Verde para nivel 1
        Color(0xFF2196F3), // Azul para nivel 2
        Color(0xFF9C27B0)  // Púrpura para nivel 3
    )
    val levelGradientColors = listOf(
        listOf(Color(0xFF10B981), Color(0xFF34D399)), // Verde para nivel 1
        listOf(Color(0xFF03A9F4), Color(0xFF2196F3)), // Azul para nivel 2
        listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))  // Púrpura para nivel 3
    )
    val levelColor = levelColors.getOrElse(level - 1) { MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) }
    val levelGradient = levelGradientColors.getOrElse(level - 1) { listOf(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }

    // Following HomeScreen design
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(levelColor.copy(alpha = 0.08f), Color.White)
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(levelColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$level",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.surface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nivel $level",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = levelColor
                        )
                    }
                    Text(
                        text = "$currency ${String.format("%.2f", detail.netCommission ?: 0.0)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = levelColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mostrar código de beneficiario
                detail.userReferralCode?.let {
                    DetailRow("Código de Beneficiario", it)
                }

                // Porcentajes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "% de distribución",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${detail.percentageOfDistribution ?: 0.0}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "% de la venta",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${detail.percentageOfSale ?: 0.0}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Estado del pago
                detail.paymentStatus?.let { status ->
                    Spacer(modifier = Modifier.height(4.dp))
                    val statusColor = when (status) {
                        "paid" -> Color(0xFF4CAF50)
                        "pending", "scheduled" -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (status) {
                                "paid" -> Icons.Default.CheckCircle
                                else -> Icons.Default.Schedule
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = statusColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Estado: ${getStatusText(status)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    }
                }

                // Nota
                detail.note?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// ====== MAIN SCREEN FUNCTIONS ======

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: String,
    viewModel: com.christelldev.easyreferplus.ui.viewmodel.TransactionDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(transactionId) {
        viewModel.loadTransactionDetail(transactionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle de Transacción",
                        color = MaterialTheme.colorScheme.surface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.errorMessage != null -> {
                    ErrorContent(
                        message = uiState.errorMessage!!,
                        onRetry = { viewModel.loadTransactionDetail(transactionId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.receipt != null -> {
                    TransactionDetailContent(
                        receipt = uiState.receipt!!
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionDetailContent(receipt: TransactionReceipt) {
    val isSale = receipt.type == "sale"
    val primaryColor = if (isSale) Color(0xFF4CAF50) else AppBlue
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con monto - Following HomeScreen design
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
            shape = RoundedCornerShape(CARD_CORNER_RADIUS),
            colors = CardDefaults.cardColors(containerColor = primaryColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSale) Icons.Default.Store else Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isSale) "Venta" else "Compra",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
                Text(
                    text = "${receipt.currency} ${String.format("%.2f", receipt.amount)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )
            }
        }

        // Información de la empresa
        DetailCard(title = "Empresa") {
            DetailRow("Nombre", receipt.companyName ?: "N/A")
            receipt.companyAddress?.let { DetailRow("Dirección", it) }
            receipt.companyPhone?.let { DetailRow("Teléfono", it) }
        }

        // Información de la transacción
        DetailCard(title = "Detalles de la Transacción") {
            DetailRow("ID", maskTransactionId(receipt.transactionId))
            DetailRow("Fecha", formatDate(receipt.transactionDate))
            receipt.referralCodeUsed?.let { DetailRow("Código de referido usado", it) }
            receipt.description?.let { DetailRow("Descripción", it) }
        }

        // Información del comprador (solo para ventas)
        if (isSale) {
            DetailCard(title = "Comprador") {
                DetailRow("Nombre", receipt.buyerName ?: "N/A")
                receipt.buyerDocument?.let { DetailRow("Documento", it) }
                receipt.buyerPhone?.let { DetailRow("Teléfono", it) }
                receipt.buyerEmail?.let { DetailRow("Email", it) }
            }
        }

        // Información de Comisiones
        receipt.commissionInfo?.let { commissionInfo ->
            CommissionDetailsCard(
                commissionInfo = commissionInfo,
                totalAmount = receipt.amount,
                currency = receipt.currency
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CommissionDetailsCard(
    commissionInfo: com.christelldev.easyreferplus.data.model.CommissionInfo,
    totalAmount: Double,
    currency: String
) {
    // Following HomeScreen design
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF10B981), Color(0xFF34D399))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Percent,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Desglose de Comisiones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider()

            // Comisión total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Comisión",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$currency ${String.format("%.2f", commissionInfo.totalCommissionAmount ?: 0.0)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }

            // Porcentaje de la empresa
            DetailRow(
                "Porcentaje empresa",
                "${commissionInfo.companyCommissionPercentage ?: 0.0}%"
            )

            // Detalles por nivel
            val details = commissionInfo.commissionDetails
            if (!details.isNullOrEmpty()) {
                Text(
                    text = "Distribución por Nivel",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                details.forEachIndexed { index, detail ->
                    CommissionLevelCard(
                        level = index + 1,
                        detail = detail,
                        currency = currency
                    )
                }
            } else {
                // Si no hay detalles, mostrar explicación
                Text(
                    text = "Esta transacción no generó comisiones de referido porque no se usó un código de referido o el vendedor no tiene referidos en la cadena.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Explicación de cómo se calcula
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "¿Cómo se calcula la comisión?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "La empresa define un porcentaje de comisión sobre el monto de la venta. " +
                        "Ese monto se distribuye entre los referidos del comprador: " +
                        "Nivel 1 (directo), Nivel 2 (referido del referido) y Nivel 3. " +
                        "El resto va a la plataforma.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
