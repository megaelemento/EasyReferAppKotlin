package com.christelldev.easyreferplus.ui.screens.driver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.data.model.DriverInvitation
import com.christelldev.easyreferplus.ui.viewmodel.DriverViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverInvitationsScreen(
    viewModel: DriverViewModel,
    onNavigateBack: () -> Unit
) {
    val invitations by viewModel.invitations.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadInvitations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invitaciones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (invitations.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No tienes invitaciones pendientes")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(invitations) { invitation ->
                    InvitationCard(
                        invitation = invitation,
                        onAccept = {
                            viewModel.acceptInvitation(invitation.id,
                                onSuccess = {
                                    scope.launch { snackbarHostState.showSnackbar("Invitación aceptada") }
                                    viewModel.loadInvitations()
                                    viewModel.loadProfile()
                                },
                                onError = { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            )
                        },
                        onReject = {
                            viewModel.rejectInvitation(invitation.id,
                                onSuccess = {
                                    scope.launch { snackbarHostState.showSnackbar("Invitación rechazada") }
                                    viewModel.loadInvitations()
                                },
                                onError = { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InvitationCard(
    invitation: DriverInvitation,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                invitation.companyName ?: "Empresa #${invitation.companyId}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Estado: ${invitation.status}",
                style = MaterialTheme.typography.bodySmall,
                color = when (invitation.status) {
                    "pending" -> MaterialTheme.colorScheme.primary
                    "accepted" -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.error
                }
            )
            if (invitation.status == "pending") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAccept, modifier = Modifier.weight(1f)) {
                        Text("Aceptar")
                    }
                    OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) {
                        Text("Rechazar")
                    }
                }
            }
        }
    }
}
