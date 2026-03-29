package com.christelldev.easyreferplus.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.*
import com.christelldev.easyreferplus.data.network.ApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject

data class OrderChatState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val orderId: Int = 0,
    val currentUserId: Int = 0,
    val otherUserName: String = "",
    val otherUserOnline: Boolean = false,
    val otherUserTyping: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val hasMore: Boolean = false,
    val isSending: Boolean = false,
)

class OrderChatViewModel(
    private val apiService: ApiService,
    private val getToken: () -> String,
    private val getWsUrl: () -> String,
    private val getCurrentUserId: () -> Int
) : ViewModel() {

    private val _state = MutableStateFlow(OrderChatState())
    val state: StateFlow<OrderChatState> = _state.asStateFlow()

    private var webSocket: WebSocket? = null
    private var typingJob: kotlinx.coroutines.Job? = null

    fun loadChat(orderId: Int, otherUserName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                orderId = orderId,
                currentUserId = getCurrentUserId(),
                otherUserName = otherUserName,
                error = null,
            )
            try {
                val token = "Bearer ${getToken()}"
                val response = apiService.getChatMessages(orderId, limit = 50, authorization = token)
                if (response.isSuccessful) {
                    val body = response.body()!!
                    _state.value = _state.value.copy(
                        isLoading = false,
                        messages = body.messages,
                        hasMore = body.hasMore,
                    )
                    // Mark as read
                    apiService.markChatMessagesRead(orderId, token)
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "Error al cargar mensajes")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Error de conexión: ${e.message}")
            }
            // Connect WebSocket
            connectWebSocket(orderId)
        }
    }

    fun loadMoreMessages() {
        val s = _state.value
        if (s.isLoading || !s.hasMore || s.messages.isEmpty()) return
        val firstId = s.messages.first().id

        viewModelScope.launch {
            try {
                val token = "Bearer ${getToken()}"
                val response = apiService.getChatMessages(s.orderId, limit = 50, beforeId = firstId, authorization = token)
                if (response.isSuccessful) {
                    val body = response.body()!!
                    _state.value = _state.value.copy(
                        messages = body.messages + s.messages,
                        hasMore = body.hasMore,
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val s = _state.value

        // Try WebSocket first
        val ws = webSocket
        if (ws != null) {
            try {
                val json = JSONObject().apply {
                    put("type", "chat_message")
                    put("message", trimmed)
                }
                ws.send(json.toString())
                // We'll add the message when we get the ack
                _state.value = s.copy(isSending = true)
                return
            } catch (_: Exception) {
                // Fall through to REST
            }
        }

        // REST fallback
        viewModelScope.launch {
            _state.value = s.copy(isSending = true)
            try {
                val token = "Bearer ${getToken()}"
                val response = apiService.sendChatMessage(s.orderId, ChatMessageRequest(trimmed), token)
                if (response.isSuccessful) {
                    val msg = response.body()!!
                    val chatMsg = ChatMessage(
                        id = msg.id,
                        senderId = msg.senderId,
                        senderName = msg.senderName,
                        message = msg.message,
                        createdAt = msg.createdAt,
                    )
                    _state.value = _state.value.copy(
                        messages = _state.value.messages + chatMsg,
                        isSending = false,
                    )
                } else {
                    _state.value = _state.value.copy(isSending = false, error = "Error al enviar mensaje")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSending = false, error = "Error de conexión")
            }
        }
    }

    fun sendTyping(isTyping: Boolean) {
        webSocket?.let { ws ->
            try {
                val json = JSONObject().apply {
                    put("type", "chat_typing")
                    put("is_typing", isTyping)
                }
                ws.send(json.toString())
            } catch (_: Exception) {}
        }
    }

    fun markAsRead() {
        viewModelScope.launch {
            try {
                val token = "Bearer ${getToken()}"
                apiService.markChatMessagesRead(_state.value.orderId, token)
                // Also tell WS
                webSocket?.let { ws ->
                    try {
                        ws.send("{\"type\":\"chat_read\"}")
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }
    }

    private fun connectWebSocket(orderId: Int) {
        if (webSocket != null) return
        val token = getToken()
        val wsUrl = getWsUrl()
        val url = "${wsUrl}ws/chat/$orderId?token=$token"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    when (json.optString("type")) {
                        "chat_message" -> {
                            val msg = ChatMessage(
                                id = json.getInt("message_id"),
                                senderId = json.getInt("sender_id"),
                                senderName = json.optString("sender_name", ""),
                                message = json.getString("message"),
                                createdAt = json.optString("timestamp"),
                            )
                            _state.value = _state.value.copy(
                                messages = _state.value.messages + msg,
                                otherUserTyping = false,
                            )
                            // Auto mark read
                            markAsRead()
                        }
                        "chat_message_ack" -> {
                            // Our message was persisted — add to list
                            val msgId = json.getInt("message_id")
                            val timestamp = json.optString("timestamp")
                            val userId = _state.value.currentUserId
                            val msg = ChatMessage(
                                id = msgId,
                                senderId = userId,
                                senderName = "",
                                message = "", // filled below
                                createdAt = timestamp,
                            )
                            // We don't have the text in the ack, so we track it differently:
                            // The ack confirms the last sent message. We already display optimistically.
                            _state.value = _state.value.copy(isSending = false)
                            // Reload to sync properly
                            refreshMessages()
                        }
                        "chat_typing" -> {
                            val isTyping = json.optBoolean("is_typing", false)
                            _state.value = _state.value.copy(otherUserTyping = isTyping)
                            // Auto-clear typing after 5s
                            typingJob?.cancel()
                            if (isTyping) {
                                typingJob = viewModelScope.launch {
                                    delay(5000)
                                    _state.value = _state.value.copy(otherUserTyping = false)
                                }
                            }
                        }
                        "chat_read" -> {
                            // Other user read our messages
                        }
                        "chat_online" -> {
                            val online = json.optBoolean("online", false)
                            _state.value = _state.value.copy(otherUserOnline = online)
                        }
                        "pong" -> { /* keep alive */ }
                    }
                } catch (e: Exception) {
                    Log.e("ChatWS", "Error parsing: ${e.message}")
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: okhttp3.Response?) {
                Log.e("ChatWS", "WebSocket failed: ${t.message}")
                webSocket = null
                viewModelScope.launch {
                    delay(3000)
                    if (_state.value.orderId > 0) {
                        connectWebSocket(_state.value.orderId)
                    }
                }
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                webSocket = null
            }
        })

        // Ping every 30s
        viewModelScope.launch {
            while (webSocket != null) {
                delay(30_000)
                try {
                    webSocket?.send("{\"type\":\"ping\"}")
                } catch (_: Exception) {}
            }
        }
    }

    private fun refreshMessages() {
        viewModelScope.launch {
            try {
                val token = "Bearer ${getToken()}"
                val response = apiService.getChatMessages(_state.value.orderId, limit = 50, authorization = token)
                if (response.isSuccessful) {
                    val body = response.body()!!
                    _state.value = _state.value.copy(
                        messages = body.messages,
                        hasMore = body.hasMore,
                    )
                }
            } catch (_: Exception) {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocket?.close(1000, "ViewModel cleared")
        webSocket = null
        typingJob?.cancel()
    }

    class Factory(
        private val apiService: ApiService,
        private val getToken: () -> String,
        private val getWsUrl: () -> String,
        private val getCurrentUserId: () -> Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OrderChatViewModel(apiService, getToken, getWsUrl, getCurrentUserId) as T
        }
    }
}
