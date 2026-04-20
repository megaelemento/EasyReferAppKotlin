package com.christelldev.easyreferplus.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.AdminWithdrawalResponse
import com.christelldev.easyreferplus.ui.theme.AppBlue
import com.christelldev.easyreferplus.ui.viewmodel.AdminWithdrawalsUiState
import com.christelldev.easyreferplus.ui.viewmodel.AdminWithdrawalsViewModel

// Constantes de diseño elegante
private val CARD_CORNER_RADIUS = 20.dp
private val CARD_ELEVATION = 8.dp
private val CARD_MARGIN_HORIZONTAL = 16.dp
private val GradientPrimary = listOf(Color(0xFF03A9F4), Color(0xFF2196F3))
private val GradientSuccess = listOf(Color(0xFF10B981), Color(0xFF34D399))
private val GradientOrange = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
private val GradientRed = listOf(Color(0xFFEF4444), Color(0xFFF87171))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWithdrawalsScreen(
    viewModel: AdminWithdrawalsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = isSystemInDarkTheme()
    val contentTint = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface

    // Mostrar mensajes de éxito/error
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
            )
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.admin_withdrawals),
                            fontWeight = FontWeight.Bold,
                            color = contentTint
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = contentTint
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                // Stats Cards
                WithdrawalsStatsRow(uiState = uiState)

                // Filter Chips
                FilterChipsRow(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = viewModel::setFilter,
                    pendingCount = uiState.pendingCount,
                    approvedCount = uiState.approvedCount,
                    rejectedCount = uiState.rejectedCount,
                    postponedCount = uiState.postponedCount
                )

                // Withdrawals List
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.withdrawals.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.no_withdrawals),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.withdrawals) { withdrawal ->
                            WithdrawalCard(
                                withdrawal = withdrawal,
                                isProcessing = uiState.isProcessingAction,
                                onApprove = { notes, ref -> viewModel.approveWithdrawal(withdrawal.id, notes, ref) },
                                onReject = { notes -> viewModel.rejectWithdrawal(withdrawal.id, notes) },
                                onPostpone = { notes -> viewModel.postponeWithdrawal(withdrawal.id, notes) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WithdrawalsStatsRow(uiState: AdminWithdrawalsUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            title = stringResource(R.string.pending),
            count = uiState.pendingCount,
            color = Color(0xFFFF9800),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = stringResource(R.string.approved),
            count = uiState.approvedCount,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = stringResource(R.string.rejected),
            count = uiState.rejectedCount,
            color = Color(0xFFF44336),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = stringResource(R.string.postponed),
            count = uiState.postponedCount,
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(title: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: String?,
    onFilterSelected: (String?) -> Unit,
    pendingCount: Int,
    approvedCount: Int,
    rejectedCount: Int,
    postponedCount: Int
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { onFilterSelected(null) },
                label = { Text(stringResource(R.string.all)) }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == "pending",
                onClick = { onFilterSelected("pending") },
                label = { Text("${stringResource(R.string.pending)} ($pendingCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFF9800).copy(alpha = 0.2f)
                )
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == "approved",
                onClick = { onFilterSelected("approved") },
                label = { Text("${stringResource(R.string.approved)} ($approvedCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                )
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == "rejected",
                onClick = { onFilterSelected("rejected") },
                label = { Text("${stringResource(R.string.rejected)} ($rejectedCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFF44336).copy(alpha = 0.2f)
                )
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == "postponed",
                onClick = { onFilterSelected("postponed") },
                label = { Text("${stringResource(R.string.postponed)} ($postponedCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF2196F3).copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
private fun WithdrawalCard(
    withdrawal: AdminWithdrawalResponse,
    isProcessing: Boolean,
    onApprove: (String?, String?) -> Unit,
    onReject: (String?) -> Unit,
    onPostpone: (String?) -> Unit
) {
    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showPostponeDialog by remember { mutableStateOf(false) }

    val statusColor = when (withdrawal.status.lowercase()) {
        "pending" -> Color(0xFFFF9800)
        "approved" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFF44336)
        "postponed" -> Color(0xFF2196F3)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    val statusIcon = when (withdrawal.status.lowercase()) {
        "pending" -> Icons.Default.Pending
        "approved" -> Icons.Default.CheckCircle
        "rejected" -> Icons.Default.Cancel
        "postponed" -> Icons.Default.Schedule
        else -> Icons.Default.AccountBalance
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = withdrawal.userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = withdrawal.userPhone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Text(
                    text = "$${String.format("%.2f", withdrawal.requestedAmount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bank Account Info
            withdrawal.bankAccount?.let { account ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${account.bankName} - ${account.accountType.uppercase()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "****${account.accountNumber.takeLast(4)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Status
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${stringResource(R.string.status)}: ${withdrawal.status.uppercase()}",
                style = MaterialTheme.typography.labelMedium,
                color = statusColor
            )

            // Notes if any
            if (!withdrawal.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stringResource(R.string.reason)}: ${withdrawal.rejectionReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Action Buttons (only for pending)
            if (withdrawal.status.lowercase() == "pending") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showApproveDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        enabled = !isProcessing
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.approve))
                    }
                    OutlinedButton(
                        onClick = { showPostponeDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isProcessing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.postpone))
                    }
                    Button(
                        onClick = { showRejectDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        ),
                        enabled = !isProcessing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.reject))
                    }
                }
            }
        }
    }

    // Approve Dialog
    if (showApproveDialog) {
        var notes by remember { mutableStateOf("") }
        var transactionRef by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = { Text(stringResource(R.string.approve_withdrawal)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = transactionRef,
                        onValueChange = { transactionRef = it },
                        label = { Text(stringResource(R.string.transaction_reference)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(stringResource(R.string.notes_optional)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onApprove(notes.ifBlank { null }, transactionRef.ifBlank { null })
                        showApproveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text(stringResource(R.string.approve))
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Reject Dialog
    if (showRejectDialog) {
        var reason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text(stringResource(R.string.reject_withdrawal)) },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(stringResource(R.string.rejection_reason)) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReject(reason.ifBlank { "Sin motivo especificado" })
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text(stringResource(R.string.reject))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Postpone Dialog
    if (showPostponeDialog) {
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPostponeDialog = false },
            title = { Text(stringResource(R.string.postpone_withdrawal)) },
            text = {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.postpone_reason)) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPostpone(notes.ifBlank { "Sin motivo especificado" })
                        showPostponeDialog = false
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPostponeDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
