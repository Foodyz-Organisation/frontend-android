package com.example.damprojectfinal.user.feature_chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.R
import com.example.damprojectfinal.core.api.ChatApiService
import com.example.damprojectfinal.core.api.ChatSocketManager
import com.example.damprojectfinal.core.api.ConversationDto
import com.example.damprojectfinal.core.api.CreateConversationDto
import com.example.damprojectfinal.core.api.MessageDto
import com.example.damprojectfinal.core.api.PeerDto
import com.example.damprojectfinal.core.api.SendMessageDto
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.core.model.ChatListItem
import com.example.damprojectfinal.core.model.ChatMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class ChatViewModel : ViewModel() {

    private val chatApiService: ChatApiService = RetrofitClient.chatApiService


    // ===== Liste des conversations (écran liste) =====
    private val _chats = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chats: StateFlow<List<ChatMessage>> = _chats

    private val _chatItems = MutableStateFlow<List<ChatListItem>>(emptyList())
    val chatItems: StateFlow<List<ChatListItem>> = _chatItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _peers = MutableStateFlow<List<PeerDto>>(emptyList())
    val peers: StateFlow<List<PeerDto>> = _peers

    private val _isStartingConversation = MutableStateFlow(false)
    val isStartingConversation: StateFlow<Boolean> = _isStartingConversation

    private val _startConversationError = MutableStateFlow<String?>(null)
    val startConversationError: StateFlow<String?> = _startConversationError

    private val _isSendingMessage = MutableStateFlow(false)
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage

    private var peersCache: Map<String, PeerDto> = emptyMap()
    private var conversations: List<ConversationDto> = emptyList()
    private var conversationRefreshJob: Job? = null
    private var messagesRefreshJob: Job? = null

    // ===== Messages temps réel (WebSocket) =====
    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    val messages: StateFlow<List<MessageDto>> = _messages

    private val _isSocketConnected = MutableStateFlow(false)
    val isSocketConnected: StateFlow<Boolean> = _isSocketConnected

    private var socketManager: ChatSocketManager? = null

    // =======================
    //      API REST
    // =======================
    fun loadConversations(authToken: String?, currentUserId: String?, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (authToken.isNullOrBlank()) {
                _errorMessage.value = "Missing authentication token"
                _chats.value = emptyList()
                _chatItems.value = emptyList()
                _isLoading.value = false
                return@launch
            }

            if (showLoading) _isLoading.value = true
            try {
                // Aligne les endpoints avec iOS: liste des conversations
                val response: List<ConversationDto> =
                    chatApiService.getConversations("Bearer $authToken")

                // Filter out conversations with null IDs
                val validConversations = response.filter { it.id != null }

                // Mapping DTO -> modèle UI
                conversations = validConversations
                _chats.value = validConversations.map { it.toChatMessage() }
                _chatItems.value = validConversations.map { it.toChatListItem(currentUserId) }

                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load conversations: ${e.message}"
            } finally {
                if (showLoading) _isLoading.value = false
            }
        }
    }

    fun loadPeers(authToken: String?, force: Boolean = false, currentUserId: String? = null) {
        viewModelScope.launch {
            if (!force && _peers.value.isNotEmpty()) return@launch
            if (authToken.isNullOrBlank()) return@launch

            try {
                val response = chatApiService.getPeers("Bearer $authToken")
                _peers.value = response.sortedBy { it.name.lowercase() }
                peersCache = response.associateBy { it.id }
                if (conversations.isNotEmpty()) {
                    _chatItems.value = conversations.map { it.toChatListItem(currentUserId) }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load peers: ${e.message}"
            }
        }
    }

    fun startAutoRefresh(authToken: String?, currentUserId: String?, intervalSeconds: Long = 10) {
        if (conversationRefreshJob != null) return
        conversationRefreshJob = viewModelScope.launch {
            while (isActive) {
                loadConversations(authToken, currentUserId, showLoading = false)
                delay(intervalSeconds * 1000)
            }
        }
    }

    fun stopAutoRefresh() {
        conversationRefreshJob?.cancel()
        conversationRefreshJob = null
    }

    fun startConversationWithPeer(
        peer: PeerDto,
        authToken: String?,
        currentUserId: String?,
        onResult: (ConversationDto?) -> Unit
    ) {
        viewModelScope.launch {
            if (authToken.isNullOrBlank()) {
                _startConversationError.value = "Missing authentication token"
                onResult(null)
                return@launch
            }

            if (currentUserId.isNullOrBlank()) {
                _startConversationError.value = "Missing current user ID"
                onResult(null)
                return@launch
            }

            _isStartingConversation.value = true
            _startConversationError.value = null
            try {
                val existing = findExistingConversation(peer.id, currentUserId)
                val conversation = existing ?: chatApiService.createConversation(
                    bearerToken = "Bearer $authToken",
                    dto = CreateConversationDto(
                        kind = "private",
                        participants = listOf(currentUserId, peer.id), // Include both users
                        title = peer.name
                    )
                    
                )

                // Validate that the conversation has a valid ID
                if (conversation.id.isNullOrBlank()) {
                    _startConversationError.value = "Backend error: conversation created without ID"
                    onResult(null)
                    return@launch
                }

                upsertConversation(conversation, currentUserId)
                onResult(conversation)
            } catch (e: Exception) {
                _startConversationError.value = e.message ?: "Unable to start chat"
                onResult(null)
            } finally {
                _isStartingConversation.value = false
            }
        }
    }

    fun sendMessage(conversationId: String, messageText: String) {
        viewModelScope.launch {
            try {
                // TODO: Appel HTTP si tu veux en plus du WebSocket
                // chatApiService.sendMessage(conversationId, SendMessageDto(content = messageText))
            } catch (e: Exception) {
                _errorMessage.value = "Failed to send message: ${e.message}"
            }
        }
    }

    // =======================
    //      WEBSOCKET
    // =======================
    fun initSocket(
        baseUrl: String,
        authToken: String,
        conversationId: String,
        socketPath: String = "/socket.io"
    ) {
        if (conversationId.isBlank()) {
            _errorMessage.value = "Conversation ID manquant"
            return
        }

        if (socketManager?.isConnected() == true) return

        socketManager = ChatSocketManager(baseUrl, authToken, socketPath).apply {

            setOnConnectionChange { connected ->
                _isSocketConnected.value = connected
                if (connected) {
                    joinConversation(conversationId)
                }
            }

            setOnError { error ->
                _errorMessage.value = error
            }

            setOnMessageReceived { msg ->
                _messages.value = _messages.value + msg
            }
        }

        socketManager?.connect()
    }

    fun sendSocketMessage(
        conversationId: String,
        text: String,
        type: String = "text"
    ) {
        if (text.isBlank()) return

        val socket = socketManager
        if (socket == null || !socket.isConnected()) {
            _errorMessage.value = "Socket non connectée"
            return
        }

        socket.sendMessage(
            conversationId = conversationId,
            content = text,
            type = type
        )
    }

    fun clearSocketMessages() {
        _messages.value = emptyList()
    }

    fun loadMessages(authToken: String?, conversationId: String, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (authToken.isNullOrBlank()) {
                _errorMessage.value = "Missing authentication token"
                return@launch
            }
            if (showLoading) {
                _isLoading.value = true
            }
            try {
                val response = chatApiService.getMessages(
                    bearerToken = "Bearer $authToken",
                    conversationId = conversationId
                )
                _messages.value = response.sortedBy { it.createdAt ?: "" }
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unable to load messages"
            } finally {
                if (showLoading) _isLoading.value = false
            }
        }
    }

    fun startMessagesAutoRefresh(
        authToken: String?,
        conversationId: String,
        intervalSeconds: Long = 5
    ) {
        if (messagesRefreshJob != null) return
        messagesRefreshJob = viewModelScope.launch {
            while (isActive) {
                loadMessages(authToken, conversationId, showLoading = false)
                delay(intervalSeconds * 1000)
            }
        }
    }

    fun stopMessagesAutoRefresh() {
        messagesRefreshJob?.cancel()
        messagesRefreshJob = null
    }

    fun sendMessageHttp(authToken: String?, conversationId: String, messageText: String) {
        viewModelScope.launch {
            val trimmed = messageText.trim()
            if (trimmed.isEmpty()) return@launch
            if (_isSendingMessage.value) return@launch
            if (authToken.isNullOrBlank()) {
                _errorMessage.value = "Missing authentication token"
                return@launch
            }

            _isSendingMessage.value = true
            _errorMessage.value = null
            try {
                val payload = SendMessageDto(content = trimmed, type = "text")
                val sentMessage = chatApiService.sendMessage(
                    bearerToken = "Bearer $authToken",
                    conversationId = conversationId,
                    dto = payload
                )
                _messages.value = (_messages.value + sentMessage).sortedBy { it.createdAt ?: "" }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unable to send message"
            } finally {
                _isSendingMessage.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        socketManager?.disconnect()
        socketManager = null
        stopAutoRefresh()
        stopMessagesAutoRefresh()
    }

    // =======================
    //   Mapping DTO -> UI
    // =======================
    private fun ConversationDto.toChatMessage(): ChatMessage {
        return ChatMessage(
            conversationId = id ?: "unknown",
            name = title?.takeIf { it.isNotBlank() } ?: "Conversation",
            message = "No message yet",
            time = updatedAt ?: createdAt ?: "",
            unreadCount = 0,
            profileImage = R.drawable.profile,  // à personnaliser plus tard
            online = false
        )
    }

    private fun ConversationDto.toChatListItem(currentUserId: String?): ChatListItem {
        val resolvedTitle = resolveTitle(currentUserId)
        return ChatListItem(
            id = id ?: "unknown",
            title = resolvedTitle,
            subtitle = summary(),
            updatedTime = formatTimestamp(updatedAt ?: createdAt),
            unreadCount = 0,
            isOnline = false,
            avatarUrl = avatarFor(currentUserId),
            initials = initialsFrom(resolvedTitle)
        )
    }

    private fun ConversationDto.resolveTitle(currentUserId: String?): String {
        if (!title.isNullOrBlank()) return title
        if (kind == "group") return "Group conversation"

        val otherId = participants.firstOrNull { it != null && it != currentUserId }
        val peerName = otherId?.let { peersCache[it]?.name }
        return peerName ?: "Conversation"
    }

    fun displayTitleFor(conversation: ConversationDto, currentUserId: String?): String {
        return conversation.resolveTitle(currentUserId)
    }

    private fun ConversationDto.summary(): String {
        return when (kind) {
            "group" -> "Group \u00b7 ${participants.size} participants"
            "private" -> if (participants.size == 2) "1-on-1 chat" else "Conversation"
            else -> "Conversation"
        }
    }

    private fun formatTimestamp(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return try {
            val instant = Instant.parse(raw)
            DateTimeFormatter.ofPattern("HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(instant)
        } catch (_: DateTimeParseException) {
            raw
        }
    }

    private fun findExistingConversation(peerId: String, currentUserId: String?): ConversationDto? {
        if (currentUserId == null) return null
        return conversations.firstOrNull { conversation ->
            conversation.kind == "private" &&
                conversation.participants.contains(currentUserId) &&
                conversation.participants.contains(peerId) &&
                conversation.participants.size == 2
        }
    }

    private fun upsertConversation(conversation: ConversationDto, currentUserId: String?) {
        val mutable = conversations.toMutableList()
        val index = mutable.indexOfFirst { it.id == conversation.id && conversation.id != null }
        if (index >= 0) {
            mutable[index] = conversation
        } else {
            mutable.add(0, conversation)
        }
        conversations = mutable
        _chats.value = conversations.map { it.toChatMessage() }
        _chatItems.value = conversations.map { it.toChatListItem(currentUserId) }
    }

    private fun ConversationDto.avatarFor(currentUserId: String?): String? {
        if (kind != "private") return null
        val otherId = participants.firstOrNull { it != currentUserId }
        return otherId?.let { peersCache[it]?.avatarUrl }
    }

    private fun initialsFrom(value: String): String {
        return value
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
            .joinToString("")
    }

    // =======================
    //          MOCK
    // =======================
    // Gardé pour debug si besoin, mais plus utilisé dans init {}
    private fun getMockChats(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                conversationId = "conv-1",
                name = "Pizza Paradise",
                message = "Your order is ready for pickup! 🍕",
                time = "2m ago",
                unreadCount = 2,
                profileImage = R.drawable.profile,
                online = true
            ),
            ChatMessage(
                conversationId = "conv-2",
                name = "Delivery Driver - Ahmed",
                message = "I'm 5 minutes away from your location",
                time = "10m ago",
                unreadCount = 1,
                profileImage = R.drawable.pasta,
                online = true
            ),
            ChatMessage(
                conversationId = "conv-3",
                name = "Sushi Palace",
                message = "Thank you for your order! 🍣",
                time = "1h ago",
                unreadCount = 0,
                profileImage = R.drawable.profile,
                online = false
            ),
            ChatMessage(
                conversationId = "conv-4",
                name = "Customer Support",
                message = "How can we help you today?",
                time = "Yesterday",
                unreadCount = 0,
                profileImage = R.drawable.profile,
                online = true
            ),
            ChatMessage(
                conversationId = "conv-5",
                name = "Burger King",
                message = "New deals available this weekend! 🍔",
                time = "2 days ago",
                unreadCount = 3,
                profileImage = R.drawable.profile,
                online = false
            )
        )
    }
}