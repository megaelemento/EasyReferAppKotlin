package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

// ─── Requests ─────────────────────────────────────────────────────────────────

data class ChatMessageRequest(
    @SerializedName("message") val message: String
)

// ─── Responses ────────────────────────────────────────────────────────────────

data class ChatMessagesResponse(
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("messages") val messages: List<ChatMessage>,
    @SerializedName("has_more") val hasMore: Boolean
)

data class ChatMessage(
    @SerializedName("id") val id: Int,
    @SerializedName("sender_id") val senderId: Int,
    @SerializedName("sender_name") val senderName: String,
    @SerializedName("message") val message: String,
    @SerializedName("read_at") val readAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class ChatSendResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("sender_id") val senderId: Int,
    @SerializedName("sender_name") val senderName: String,
    @SerializedName("message") val message: String,
    @SerializedName("created_at") val createdAt: String? = null
)

data class ChatMarkReadResponse(
    @SerializedName("marked_read") val markedRead: Int
)

data class ChatUnreadResponse(
    @SerializedName("unread_count") val unreadCount: Int
)
