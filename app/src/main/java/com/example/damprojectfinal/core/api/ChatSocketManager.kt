package com.example.damprojectfinal.core.api

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class ChatSocketManager(
    private val baseUrl: String,   // ex: "http://10.0.2.2:3000"
    private val authToken: String, // JWT
    private val socketPath: String = "/socket.io"
) {
    private var socket: Socket? = null
    private val TAG = "ChatSocketManager"

    // Callbacks
    private var onMessageReceived: ((MessageDto) -> Unit)? = null
    private var onConnectionChange: ((Boolean) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    fun connect() {
        try {
            val options = IO.Options.builder()
                .setAuth(mapOf("token" to authToken))
                .setPath(socketPath)
                .setReconnection(true)
                .setReconnectionDelay(1000)
                .setReconnectionDelayMax(5000)
                .setReconnectionAttempts(Integer.MAX_VALUE)
                .build()

            socket = IO.socket(baseUrl, options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Connected to chat server")
                onConnectionChange?.invoke(true)
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Disconnected from chat server")
                onConnectionChange?.invoke(false)
            }

            socket?.on("new_message") { args ->
                try {
                    val jsonObj = args[0] as? JSONObject
                    if (jsonObj != null) {
                        val message = parseMessage(jsonObj)
                        onMessageReceived?.invoke(message)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message", e)
                    onError?.invoke("Failed to parse message: ${e.message}")
                }
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = if (args.isNotEmpty()) args[0].toString() else "Unknown error"
                Log.e(TAG, "Connection error: $error")
                onError?.invoke("Connection error: $error")
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create socket connection", e)
            onError?.invoke("Socket initialization failed: ${e.message}")
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }

    fun sendMessage(
        conversationId: String,
        content: String,
        type: String = "text",
        meta: Map<String, String>? = null
    ) {
        try {
            val payload = JSONObject().apply {
                put("conversationId", conversationId)
                put("content", content)
                put("type", type)
                meta?.let { put("meta", JSONObject(it)) }
            }
            socket?.emit("send_message", payload)
            Log.d(TAG, "Message sent to conversation: $conversationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            onError?.invoke("Failed to send message: ${e.message}")
        }
    }

    fun joinConversation(conversationId: String) {
        try {
            val payload = JSONObject().apply {
                put("conversationId", conversationId)
            }
            socket?.emit("join_conversation", payload)
            Log.d(TAG, "Joined conversation: $conversationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error joining conversation", e)
            onError?.invoke("Failed to join conversation: ${e.message}")
        }
    }

    fun setOnMessageReceived(listener: (MessageDto) -> Unit) {
        onMessageReceived = listener
    }

    fun setOnConnectionChange(listener: (Boolean) -> Unit) {
        onConnectionChange = listener
    }

    fun setOnError(listener: (String) -> Unit) {
        onError = listener
    }

    fun isConnected(): Boolean = socket?.connected() ?: false

    private fun parseMessage(json: JSONObject): MessageDto {
        val conversationId = when {
            json.optJSONObject("conversation") != null ->
                json.optJSONObject("conversation")?.optString("_id", "") ?: ""
            else -> json.optString("conversation", "")
        }

        val senderId = when {
            json.optJSONObject("sender") != null ->
                json.optJSONObject("sender")?.optString("_id", "") ?: ""
            else -> json.optString("sender", "")
        }

        return MessageDto(
            id = json.optString("_id", ""),
            conversationId = conversationId,
            senderId = senderId,
            content = json.optString("content", ""),
            type = json.optString("type", "text"),
            createdAt = json.optString("createdAt", ""),
            meta = null
        )
    }
}
