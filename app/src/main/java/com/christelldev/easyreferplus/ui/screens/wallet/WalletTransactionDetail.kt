package com.christelldev.easyreferplus.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.data.model.WalletStatementItem
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    item: WalletStatementItem,
    onDismiss: () -> Unit
) {
    val isSent = item.type == "sent"
    val isDark = isSystemInDarkTheme()
    val accentColor = if (isSent) Color(0xFFEF4444) else Color(0xFF10B981)
    val iconBg = accentColor.copy(alpha = if (isDark) 0.18f else 0.12f)
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.08f)
                       else Color.Black.copy(alpha = 0.06f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // ── Header ─────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSent) Icons.Default.ArrowUpward
                                      else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (isSent) "Transferencia enviada"
                               else "Transferencia recibida",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatDetailDate(item.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = labelColor
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor)
            Spacer(Modifier.height(8.dp))

            // ── Monto grande centrado ───────────────────────────────────
            Text(
                text = "${if (isSent) "-" else "+"} \$${String.format("%.2f", item.amount)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = dividerColor)
            Spacer(Modifier.height(4.dp))

            // ── Filas de detalle ────────────────────────────────────────
            val counterpartLabel = if (isSent) "Destinatario" else "Remitente"
            DetailRow(counterpartLabel, item.counterpartName, labelColor)
            DetailRow("Teléfono", formatDetailPhone(item.counterpartPhone), labelColor)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = dividerColor
            )

            DetailRow(
                label = "Saldo después",
                value = "\$${String.format("%.2f", item.balanceAfter)}",
                labelColor = labelColor
            )

            if (!item.description.isNullOrBlank()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = dividerColor
                )
                DetailRow("Descripción", item.description, labelColor)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = dividerColor
            )

            DetailRow(
                label = "N° de referencia",
                value = item.id.toString().padStart(8, '0'),
                labelColor = labelColor
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color = Color.Unspecified,
    valueBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = labelColor,
            modifier = Modifier.weight(0.45f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (valueBold) FontWeight.SemiBold else FontWeight.Normal,
            color = valueColor,
            modifier = Modifier.weight(0.55f),
            textAlign = TextAlign.End
        )
    }
}

private fun formatDetailDate(iso: String): String = try {
    val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(iso)
        ?: return iso
    val date = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "EC")).format(input)
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(input)
    "$date · $time"
} catch (_: Exception) { iso.take(16) }

private fun formatDetailPhone(phone: String): String =
    if (phone.startsWith("+593") && phone.length >= 13) "0${phone.drop(4)}" else phone
