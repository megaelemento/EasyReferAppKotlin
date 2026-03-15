package com.christelldev.easyreferplus.ui.screens.qr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.ProcessReceiptResponse
import com.christelldev.easyreferplus.ui.theme.AppBlue

// Constants for consistent styling - Following HomeScreen design
private val CARD_CORNER_RADIUS = 20.dp
private val CARD_ELEVATION = 8.dp

// Modern color palette - Following HomeScreen design
private val GradientPrimary = listOf(Color(0xFF03A9F4), Color(0xFF2196F3))
private val GradientSuccess = listOf(Color(0xFF10B981), Color(0xFF34D399))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    receipt: ProcessReceiptResponse,
    onDone: () -> Unit,
    onShare: () -> Unit = {}
) {
    val isSale = receipt.transactionType == "sale"
    val primaryColor = if (isSale) Color(0xFF4CAF50) else AppBlue

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.transaction_completed),
                        color = MaterialTheme.colorScheme.surface,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Success Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Success Message
            Text(
                text = if (isSale) "¡Venta Registrada!" else "¡Compra Exitosa!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            Text(
                text = "ID: ${receipt.getDisplayId()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Amount Card - Following HomeScreen design with gradient
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
                shape = RoundedCornerShape(CARD_CORNER_RADIUS),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = if (isSale) GradientSuccess else GradientPrimary
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.total_amount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "${receipt.currency} ${String.format("%.2f", receipt.amount)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.surface
                        )
                        if (receipt.referralCodeUsed != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Referido: ${receipt.referralCodeUsed}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction Details - Following HomeScreen design
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
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Detalles de la Transacción",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Company Info
                    ReceiptDetailRow(
                        icon = Icons.Default.Store,
                        label = "Empresa",
                        value = receipt.companyName
                    )

                    // Fecha
                    receipt.transactionDate?.let { date ->
                        val formattedDate = try {
                            date.replace("T", " ").substringBefore(".")
                        } catch (e: Exception) { date }
                        ReceiptDetailRow(
                            icon = Icons.Default.Schedule,
                            label = "Fecha",
                            value = formattedDate
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir")
                }

                Button(
                    onClick = onDone,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Finalizar")
                }
            }
        }
    }
}

@Composable
private fun ReceiptDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
