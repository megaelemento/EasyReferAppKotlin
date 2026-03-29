package com.christelldev.easyreferplus.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christelldev.easyreferplus.data.model.ChatMessage
import com.christelldev.easyreferplus.ui.viewmodel.OrderChatState
import com.christelldev.easyreferplus.ui.viewmodel.OrderChatViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderChatScreen(
    orderId: Int,
    otherUserName: String,
    isDriver: Boolean,
    viewModel: OrderChatViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.loadChat(orderId, otherUserName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(otherUserName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        val statusText = when {
                            state.otherUserTyping -> "Escribiendo..."
                            state.otherUserOnline -> "En línea"
                            else -> "Pedido #$orderId"
                        }
                        Text(
                            statusText,
                            fontSize = 12.sp,
                            color = if (state.otherUserTyping) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            ChatInputBar(
                quickMessages = if (isDriver) driverQuickMessages else buyerQuickMessages,
                isSending = state.isSending,
                onSend = { viewModel.sendMessage(it) },
                onTyping = { viewModel.sendTyping(it) },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null && state.messages.isEmpty()) {
                Text(
                    state.error!!,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            } else {
                ChatMessagesList(
                    messages = state.messages,
                    currentUserId = state.currentUserId,
                    hasMore = state.hasMore,
                    onLoadMore = { viewModel.loadMoreMessages() },
                )
            }
        }
    }
}

@Composable
private fun ChatMessagesList(
    messages: List<ChatMessage>,
    currentUserId: Int,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    if (messages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("💬", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "No hay mensajes aún",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Envía un mensaje para iniciar la conversación",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (hasMore) {
            item {
                TextButton(
                    onClick = onLoadMore,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cargar mensajes anteriores")
                }
            }
        }

        items(messages, key = { it.id }) { msg ->
            val isMine = msg.senderId == currentUserId
            ChatBubble(message = msg, isMine = isMine)
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage, isMine: Boolean) {
    val bubbleColor = if (isMine)
        Color(0xFF1565C0)
    else
        MaterialTheme.colorScheme.surfaceVariant

    val textColor = if (isMine) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMine) 16.dp else 4.dp,
                        bottomEnd = if (isMine) 4.dp else 16.dp,
                    )
                )
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                message.message,
                color = textColor,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                formatTime(message.createdAt),
                color = textColor.copy(alpha = 0.6f),
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    quickMessages: List<String>,
    isSending: Boolean,
    onSend: (String) -> Unit,
    onTyping: (Boolean) -> Unit,
) {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Quick messages
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            quickMessages.forEach { qm ->
                SuggestionChip(
                    onClick = { onSend(qm) },
                    label = { Text(qm, fontSize = 11.sp, maxLines = 1) },
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
        }

        // Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onTyping(it.isNotEmpty())
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje...") },
                maxLines = 3,
                shape = RoundedCornerShape(24.dp),
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSend(text.trim())
                        text = ""
                        onTyping(false)
                    }
                },
                enabled = text.isNotBlank() && !isSending,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF1565C0),
                ),
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, "Enviar", tint = Color.White)
                }
            }
        }
    }
}

private val buyerQuickMessages = listOf(
    "Ya estoy afuera",
    "¿Cuánto falta?",
    "Cambio de dirección",
)

private val driverQuickMessages = listOf(
    "Ya llegué",
    "No encuentro la dirección",
    "Estoy en la puerta",
)

private fun formatTime(isoDate: String?): String {
    if (isoDate == null) return ""
    return try {
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
        )
        val outFmt = SimpleDateFormat("h:mm a", Locale.getDefault())
        for (fmt in formats) {
            fmt.timeZone = TimeZone.getTimeZone("UTC")
            try {
                val date = fmt.parse(isoDate) ?: continue
                outFmt.timeZone = TimeZone.getDefault()
                return outFmt.format(date)
            } catch (_: Exception) {}
        }
        ""
    } catch (_: Exception) { "" }
}
