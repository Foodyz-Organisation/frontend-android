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
    
    // WebRTC Callbacks
    private var onCallMade: ((JSONObject) -> Unit)? = null
    private var onAnswerMade: ((JSONObject) -> Unit)? = null
    private var onIceCandidateReceived: ((JSONObject) -> Unit)? = null
    private var onCallEnded: ((JSONObject) -> Unit)? = null
    private var onCallDeclined: ((JSONObject) -> Unit)? = null

    fun connect() {
        try {
            val options = IO.Options.builder()
                .setAuth(mapOf("token" to authToken))
                .setPath(socketPath)
                .setTransports(arrayOf("websocket")) // Force websocket transport
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

            // WebRTC Events
            socket?.on("call_made") { args ->
                val data = args[0] as? JSONObject
                if (data != null) onCallMade?.invoke(data)
            }

            socket?.on("answer_made") { args ->
                val data = args[0] as? JSONObject
                if (data != null) onAnswerMade?.invoke(data)
            }

            socket?.on("ice_candidate_received") { args ->
                val data = args[0] as? JSONObject
                if (data != null) onIceCandidateReceived?.invoke(data)
            }
            
            socket?.on("call_ended") { args ->
                val data = args[0] as? JSONObject
                if (data != null) onCallEnded?.invoke(data)
            }

            socket?.on("call_declined") { args ->
                val data = args[0] as? JSONObject
                if (data != null) onCallDeclined?.invoke(data)
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

    // WebRTC Signaling Methods
    fun emitCallUser(conversationId: String, offer: JSONObject) {
        val payload = JSONObject().apply {
            put("conversationId", conversationId)
            put("offer", offer)
        }
        socket?.emit("call_user", payload)
    }

    fun emitMakeAnswer(toSocketId: String, answer: JSONObject) {
        val payload = JSONObject().apply {
            put("to", toSocketId)
            put("answer", answer)
        }
        socket?.emit("make_answer", payload)
    }

    fun emitIceCandidate(toSocketId: String, candidate: JSONObject) {
        val payload = JSONObject().apply {
            put("to", toSocketId)
            put("candidate", candidate)
        }
        socket?.emit("ice_candidate", payload)
    }
    
    fun emitEndCall(conversationId: String) {
        val payload = JSONObject().apply {
            put("conversationId", conversationId)
        }
        socket?.emit("end_call", payload)
    }

    fun setOnCallMade(listener: (JSONObject) -> Unit) { onCallMade = listener }
    fun setOnAnswerMade(listener: (JSONObject) -> Unit) { onAnswerMade = listener }
    fun setOnIceCandidateReceived(listener: (JSONObject) -> Unit) { onIceCandidateReceived = listener }
    fun setOnCallEnded(listener: (JSONObject) -> Unit) { onCallEnded = listener }
    fun setOnCallDeclined(listener: (JSONObject) -> Unit) { onCallDeclined = listener }
    
    fun emit(event: String, data: JSONObject) {
        socket?.emit(event, data)
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
            meta = null,
            hasBadWords = json.optBoolean("hasBadWords", false),
            moderatedContent = if (json.has("moderatedContent") && !json.isNull("moderatedContent")) json.getString("moderatedContent") else null,
            isSpam = json.optBoolean("isSpam", false),
            spamConfidence = json.optDouble("spamConfidence", 0.0)
        )
    }
}
