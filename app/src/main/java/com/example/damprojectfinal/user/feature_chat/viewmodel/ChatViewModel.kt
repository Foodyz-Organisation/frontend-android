package com.example.damprojectfinal.user.feature_chat.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.R
import com.example.damprojectfinal.core.api.ChatApiService
import com.example.damprojectfinal.core.api.ChatSocketManager
import com.example.damprojectfinal.core.api.ConversationDto
import com.example.damprojectfinal.core.api.CreateConversationDto
import com.example.damprojectfinal.core.api.MessageDto
import com.example.damprojectfinal.core.api.PeerDto
import com.example.damprojectfinal.core.api.SendMessageDto
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.api.UserApiService
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.core.model.ChatListItem
import com.example.damprojectfinal.core.model.ChatMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ChatViewModel(
    private val tokenManager: TokenManager? = null
) : ViewModel() {

    private val chatApiService: ChatApiService = RetrofitClient.chatApiService
    private val userApiService: UserApiService by lazy {
        UserApiService(tokenManager ?: throw IllegalStateException("TokenManager is required"))
    }

    // Factory for creating ChatViewModel with TokenManager
    class Factory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                return ChatViewModel(tokenManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


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

    private val _deleteSuccess = MutableStateFlow<Boolean>(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess

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

    // ===== WebRTC =====
    private var webRtcClient: com.example.damprojectfinal.core.webrtc.WebRtcClient? = null
    private val _incomingCall = MutableStateFlow<Boolean>(false)
    val incomingCall: StateFlow<Boolean> = _incomingCall

    private val _isInCall = MutableStateFlow<Boolean>(false)
    val isInCall: StateFlow<Boolean> = _isInCall

    private val _isVideoCall = MutableStateFlow<Boolean>(false)
    val isVideoCall: StateFlow<Boolean> = _isVideoCall

    private val _remoteVideoTrackStream = MutableStateFlow<org.webrtc.MediaStream?>(null)
    val remoteVideoTrackStream: StateFlow<org.webrtc.MediaStream?> = _remoteVideoTrackStream

    private val _isMicMuted = MutableStateFlow(false)
    val isMicMuted: StateFlow<Boolean> = _isMicMuted

    private val _isVideoMuted = MutableStateFlow(false)
    val isVideoMuted: StateFlow<Boolean> = _isVideoMuted

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn

    // Context needed for WebRTC initialization - passed via initWebRtc
    private var applicationContext: android.content.Context? = null
    private var eglBaseContext: org.webrtc.EglBase.Context? = null

    private var pendingOffer: org.json.JSONObject? = null
    private var callerSocketId: String? = null
    
    // Buffer for ICE candidates generated before we know the peer's socket ID (for the caller)
    private val pendingIceCandidates = mutableListOf<org.json.JSONObject>()
    private var currentCallConversationId: String? = null

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
                // ALWAYS load and enrich peers for fresh data
                try {
                    val peersResponse = chatApiService.getPeers("Bearer $authToken")
                    Log.d("ChatViewModel", "✅ Loaded ${peersResponse.size} peers from /chat/peers")
                    
                    // Enrich peer data with real user/professional information
                    val enrichedPeers = peersResponse.map { peer ->
                        try {
                            // Check if peer is a professional or a user
                            val displayName: String
                            val avatarUrl: String?
                            
                            if (peer.kind == "professional") {
                                // Fetch professional data
                                val proResponse = RetrofitClient.professionalApiService.getProfessionalAccount(peer.id)
                                displayName = proResponse.fullName ?: peer.name
                                avatarUrl = proResponse.profilePictureUrl ?: peer.avatarUrl
                            } else {
                                // Fetch user data
                                val userResponse = userApiService.getUserById(peer.id, authToken)
                                displayName = userResponse.username.ifEmpty { peer.name }
                                avatarUrl = userResponse.profilePictureUrl ?: peer.avatarUrl
                            }
                            
                            val enrichedPeer = peer.copy(
                                name = displayName,
                                avatarUrl = avatarUrl
                            )
                            Log.d("ChatViewModel", "  ✅ Enriched peer (${peer.kind}): id=${peer.id}, name='$displayName', avatarUrl='$avatarUrl'")
                            enrichedPeer
                        } catch (e: Exception) {
                            Log.w("ChatViewModel", "  ⚠️ Failed to enrich peer ${peer.id}: ${e.message}")
                            peer
                        }
                    }
                    
                    _peers.value = enrichedPeers.sortedBy { it.name.lowercase() }
                    peersCache = enrichedPeers.associateBy { it.id }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "❌ Failed to load peers: ${e.message}", e)
                }

                // Aligne les endpoints avec iOS: liste des conversations
                val response: List<ConversationDto> =
                    chatApiService.getConversations("Bearer $authToken")

                Log.d("ChatViewModel", "✅ Loaded ${response.size} conversations:")
                response.forEach { conv ->
                    Log.d("ChatViewModel", "  - Conv: id=${conv.id}, kind=${conv.kind}, title='${conv.title}', participants=${conv.participants}")
                }

                // Filter out conversations with null IDs
                val validConversations = response.filter { it.id != null }

                // Mapping DTO -> modèle UI
                conversations = validConversations
                _chats.value = validConversations.map { it.toChatMessage() }
                _chatItems.value = validConversations.map { it.toChatListItem(currentUserId) }
                
                Log.d("ChatViewModel", "✅ Mapped to ${_chatItems.value.size} chat items:")
                _chatItems.value.forEach { item ->
                    Log.d("ChatViewModel", "  - ChatItem: title='${item.title}', avatarUrl='${item.avatarUrl}', initials='${item.initials}'")
                }

                // Load last message for each conversation
                validConversations.forEach { conversation ->
                    conversation.id?.let { id ->
                        launch {
                            try {
                                val messages = chatApiService.getMessages(
                                    bearerToken = "Bearer $authToken",
                                    conversationId = id,
                                    limit = 1
                                )
                                val lastMessage = messages.lastOrNull()
                                if (lastMessage != null) {
                                    updateConversationLastMessage(id, lastMessage, currentUserId)
                                }
                            } catch (e: Exception) {
                                // Silently fail for individual conversation messages
                            }
                        }
                    }
                }

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
                val peersResponse = chatApiService.getPeers("Bearer $authToken")
                Log.d(TAG, "✅ loadPeers: Loaded ${peersResponse.size} peers from /chat/peers")
                
                // Enrich peer data with real user/professional information
                val enrichedPeers = peersResponse.map { peer ->
                    try {
                        val displayName: String
                        val avatarUrl: String?
                        
                        if (peer.kind == "professional") {
                            // Fetch professional data
                            val proResponse = RetrofitClient.professionalApiService.getProfessionalAccount(peer.id)
                            displayName = proResponse.fullName ?: peer.name
                            avatarUrl = proResponse.profilePictureUrl ?: peer.avatarUrl
                        } else {
                            // Fetch user data
                            val userResponse = userApiService.getUserById(peer.id, authToken)
                            displayName = userResponse.username.ifEmpty { peer.name }
                            avatarUrl = userResponse.profilePictureUrl ?: peer.avatarUrl
                        }
                        
                        val enrichedPeer = peer.copy(
                            name = displayName,
                            avatarUrl = avatarUrl
                        )
                        Log.d(TAG, "  ✅ Enriched peer (${peer.kind}): id=${peer.id}, name='$displayName', avatarUrl='$avatarUrl'")
                        enrichedPeer
                    } catch (e: Exception) {
                        Log.w(TAG, "  ⚠️ Failed to enrich peer ${peer.id}: ${e.message}")
                        peer
                    }
                }
                
                _peers.value = enrichedPeers.sortedBy { it.name.lowercase() }
                peersCache = enrichedPeers.associateBy { it.id }
                
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
                // Update conversation with this new message
                updateConversationLastMessage(conversationId, msg, null)
            }

            // WebRTC Signaling
            setOnCallMade { data ->
                // Incoming call
                _incomingCall.value = true
                // Store offer to answer later
                pendingOffer = data.optJSONObject("offer")
                callerSocketId = data.optString("socket")
            }

            setOnAnswerMade { data ->
                val answerJson = data.optJSONObject("answer")
                // Capture the socket ID of the person checking the call (callee)
                val answererSocketId = data.optString("socket")
                if (answererSocketId.isNotBlank()) {
                    callerSocketId = answererSocketId
                    Log.d("ChatViewModel", "✅ Captured answerer socket ID: $callerSocketId")
                    
                    // Flush pending ICE candidates
                    if (pendingIceCandidates.isNotEmpty()) {
                        Log.d("ChatViewModel", "🚀 Flushing ${pendingIceCandidates.size} pending ICE candidates")
                        pendingIceCandidates.forEach { candidate ->
                            socketManager?.emitIceCandidate(callerSocketId!!, candidate)
                        }
                        pendingIceCandidates.clear()
                    }
                }
                
                answerJson?.let {
                    val sdp = org.webrtc.SessionDescription(
                        org.webrtc.SessionDescription.Type.ANSWER,
                        it.optString("sdp")
                    )
                    webRtcClient?.onRemoteSessionReceived(sdp)
                }
            }

            setOnIceCandidateReceived { data ->
                val candidateJson = data.optJSONObject("candidate")
                candidateJson?.let {
                    val candidate = org.webrtc.IceCandidate(
                        it.optString("sdpMid"),
                        it.optInt("sdpMLineIndex"),
                        it.optString("candidate")
                    )
                    webRtcClient?.addIceCandidate(candidate)
                }
            }

            setOnCallEnded {
                endCall()
            }

            setOnCallDeclined {
                viewModelScope.launch {
                    endCall()
                }
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
            content = com.example.damprojectfinal.core.utils.BadWordsFilter.moderate(text),
            type = type
        )
    }

    fun clearSocketMessages() {
        _messages.value = emptyList()
    }

    fun loadMessages(authToken: String?, conversationId: String, showLoading: Boolean = true, currentUserId: String? = null) {
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
                
                // Update conversation with last message
                val lastMessage = response.maxByOrNull { it.createdAt ?: "" }
                if (lastMessage != null) {
                    updateConversationLastMessage(conversationId, lastMessage, currentUserId)
                }
                
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
        intervalSeconds: Long = 5,
        currentUserId: String? = null
    ) {
        if (messagesRefreshJob != null) return
        messagesRefreshJob = viewModelScope.launch {
            while (isActive) {
                loadMessages(authToken, conversationId, showLoading = false, currentUserId = currentUserId)
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
                // Filter bad words on the client side before sending
                val moderatedText = com.example.damprojectfinal.core.utils.BadWordsFilter.moderate(trimmed)
                val payload = SendMessageDto(content = moderatedText, type = "text")
                val sentMessage = chatApiService.sendMessage(
                    bearerToken = "Bearer $authToken",
                    conversationId = conversationId,
                    dto = payload
                )
                Log.d("ChatViewModel", "✅ Message sent: ${sentMessage.content}, id=${sentMessage.id}, createdAt=${sentMessage.createdAt}")
                
                // If createdAt is null, assume it's new and use current time or just ensure it's at the end
                val safeMessage = if (sentMessage.createdAt.isNullOrBlank()) {
                     // Generate a simplified ISO-like string for sorting if missing
                     val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { 
                         timeZone = TimeZone.getTimeZone("UTC") 
                     }.format(java.util.Date())
                     sentMessage.copy(createdAt = now)
                } else {
                    sentMessage
                }

                _messages.value = (_messages.value + safeMessage).sortedBy { it.createdAt ?: "" }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unable to send message"
            } finally {
                _isSendingMessage.value = false
            }
        }
    }

    fun deleteConversation(authToken: String?, conversationId: String, currentUserId: String?) {
        viewModelScope.launch {
            if (authToken.isNullOrBlank()) {
                _errorMessage.value = "Missing authentication token"
                return@launch
            }

            try {
                val response = chatApiService.deleteConversation(
                    bearerToken = "Bearer $authToken",
                    conversationId = conversationId
                )
                
                if (response.isSuccessful) {
                    // Remove from local state
                    _chatItems.value = _chatItems.value.filter { it.id != conversationId }
                    _chats.value = _chats.value.filter { it.conversationId != conversationId }
                    conversations = conversations.filter { it.id != conversationId }
                    _errorMessage.value = null
                    _deleteSuccess.value = true
                } else {
                    _errorMessage.value = "Failed to delete conversation"
                    _deleteSuccess.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete conversation: ${e.message}"
                _deleteSuccess.value = false
            }
        }
    }

    fun resetDeleteSuccess() {
        _deleteSuccess.value = false
    }

    override fun onCleared() {
        super.onCleared()
        webRtcClient?.close()
        webRtcClient = null
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
            message = lastMessage?.content ?: "No message yet",
            time = updatedAt ?: createdAt ?: "",
            unreadCount = unreadCount,
            profileImage = R.drawable.profile,  // à personnaliser plus tard
            online = false
        )
    }

    private fun ConversationDto.toChatListItem(currentUserId: String?): ChatListItem {
        val resolvedTitle = resolveTitle(currentUserId)
        return ChatListItem(
            id = id ?: "unknown",
            title = resolvedTitle,
            subtitle = lastMessage?.content ?: summary(),
            updatedTime = formatTimestamp(updatedAt ?: createdAt),
            unreadCount = unreadCount,
            isOnline = false,
            avatarUrl = avatarFor(currentUserId),
            initials = initialsFrom(resolvedTitle)
        )
    }

    private fun ConversationDto.resolveTitle(currentUserId: String?): String {
        // For private chats, ALWAYS use the peer's name from cache, ignore backend title
        if (kind == "private") {
            val otherId = participants.firstOrNull { it != null && it != currentUserId }
            Log.d("ChatViewModel", "🔍 Resolving title: currentUserId=$currentUserId, otherId=$otherId, participants=$participants")
            val peer = otherId?.let { peersCache[it] }
            Log.d("ChatViewModel", "🔍 Found peer: name='${peer?.name}', email='${peer?.email}', avatarUrl='${peer?.avatarUrl}'")
            if (peer?.name != null) {
                Log.d("ChatViewModel", "✅ Using peer name: '${peer.name}'")
                return peer.name
            }
        }
        
        // For group chats, use the title if available
        if (!title.isNullOrBlank()) {
            Log.d("ChatViewModel", "⚠️ Falling back to backend title: '$title'")
            return title
        }
        if (kind == "group") return "Group conversation"
        
        Log.d("ChatViewModel", "⚠️ No title found, using default 'Conversation'")
        return "Conversation"
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
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = dateFormat.parse(raw.substringBefore(".").substringBefore("Z")) ?: return raw
            
            val now = Calendar.getInstance()
            val messageDate = Calendar.getInstance().apply { time = date }
            
            val isToday = now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                          now.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR)
            
            val isYesterday = now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                              now.get(Calendar.DAY_OF_YEAR) - 1 == messageDate.get(Calendar.DAY_OF_YEAR)

            if (isToday) {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            } else if (isYesterday) {
                "Yesterday"
            } else if (now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR)) {
                SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
            } else {
                SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
            }
        } catch (e: Exception) {
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

    private fun updateConversationLastMessage(conversationId: String, message: MessageDto, currentUserId: String?) {
        val mutable = conversations.toMutableList()
        val index = mutable.indexOfFirst { it.id == conversationId }
        if (index >= 0) {
            val updated = mutable[index].copy(
                lastMessage = message,
                updatedAt = message.createdAt
            )
            mutable[index] = updated
            conversations = mutable
            _chats.value = conversations.map { it.toChatMessage() }
            _chatItems.value = conversations.map { it.toChatListItem(currentUserId) }
        }
    }

    fun markConversationAsRead(conversationId: String, currentUserId: String?) {
        val mutable = conversations.toMutableList()
        val index = mutable.indexOfFirst { it.id == conversationId }
        if (index >= 0) {
            val updated = mutable[index].copy(unreadCount = 0)
            mutable[index] = updated
            conversations = mutable
            _chats.value = conversations.map { it.toChatMessage() }
            _chatItems.value = conversations.map { it.toChatListItem(currentUserId) }
        }
    }

    private fun ConversationDto.avatarFor(currentUserId: String?): String? {
        if (kind != "private") return null
        val otherId = participants.firstOrNull { it != currentUserId }
        val avatarUrl = otherId?.let { peersCache[it]?.avatarUrl }
        Log.d("ChatViewModel", "🖼️ Avatar for otherId=$otherId: '$avatarUrl'")
        return avatarUrl
    }

    // Get profile picture URL for a specific conversation
    fun getProfilePictureUrl(conversationId: String?, currentUserId: String?): String? {
        if (conversationId == null) return null
        val conversation = conversations.firstOrNull { it.id == conversationId }
        return conversation?.avatarFor(currentUserId)
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
    //      WebRTC
    // =======================

    fun initWebRtc(context: android.content.Context) {
        this.applicationContext = context.applicationContext
        val eglBase = org.webrtc.EglBase.create()
        this.eglBaseContext = eglBase.eglBaseContext

        webRtcClient = com.example.damprojectfinal.core.webrtc.WebRtcClient(
            context = context,
            eglBaseContext = eglBase.eglBaseContext,
            onIceCandidate = { candidate ->
                val json = org.json.JSONObject().apply {
                    put("sdpMid", candidate.sdpMid)
                    put("sdpMLineIndex", candidate.sdpMLineIndex)
                    put("candidate", candidate.sdp)
                }
                callerSocketId?.let { socketManager?.emitIceCandidate(it, json) }
            },
            onSessionDescription = { sdp ->
                val json = org.json.JSONObject().apply {
                    put("type", sdp.type.canonicalForm())
                    put("sdp", sdp.description)
                }

                if (sdp.type == org.webrtc.SessionDescription.Type.OFFER) {
                    currentCallConversationId?.let { cid ->
                        socketManager?.emitCallUser(cid, json)
                    }
                } else {
                    callerSocketId?.let { socketManager?.emitMakeAnswer(it, json) }
                }
            }
        )
    }

    fun startCall(conversationId: String, isVideo: Boolean = false) {
        android.util.Log.d("ChatViewModel", "startCall called: conversationId=$conversationId, isVideo=$isVideo")
        
        if (webRtcClient == null && applicationContext != null) {
            android.util.Log.d("ChatViewModel", "Initializing WebRTC client")
            initWebRtc(applicationContext!!)
        }

        android.util.Log.d("ChatViewModel", "Setting call states: isInCall=true, isVideoCall=$isVideo")
        _isInCall.value = true
        _isVideoCall.value = isVideo
        
        android.util.Log.d("ChatViewModel", "Starting local stream")
        webRtcClient?.startLocalStream(isVideo = isVideo)

        android.util.Log.d("ChatViewModel", "Creating peer connection")
        webRtcClient?.createPeerConnection(object : org.webrtc.PeerConnection.Observer {
            override fun onSignalingChange(p0: org.webrtc.PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: org.webrtc.PeerConnection.IceConnectionState?) {
                p0?.let { Log.d("ChatViewModel", "❄️ ICE Connection State Change: $it") }
            }
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: org.webrtc.PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(p0: org.webrtc.IceCandidate?) {
                p0?.let { candidate ->
                    val json = org.json.JSONObject().apply {
                        put("sdpMid", candidate.sdpMid)
                        put("sdpMLineIndex", candidate.sdpMLineIndex)
                        put("candidate", candidate.sdp)
                    }
                    
                    if (callerSocketId != null) {
                        Log.d("ChatViewModel", "📤 Sending ICE candidate immediately")
                        socketManager?.emitIceCandidate(callerSocketId!!, json)
                    } else {
                        Log.d("ChatViewModel", "⏳ Buffering ICE candidate (no peer socket ID yet)")
                        pendingIceCandidates.add(json)
                    }
                }
            }
            override fun onIceCandidatesRemoved(p0: Array<out org.webrtc.IceCandidate>?) {}
            override fun onAddStream(p0: org.webrtc.MediaStream?) {
                // Handle remote stream
                p0?.let { stream ->
                    Log.d("ChatViewModel", "🎥 onAddStream: Remote stream received. Video tracks: ${stream.videoTracks.size}")
                    if (stream.videoTracks.isNotEmpty()) {
                        // Notify UI to attach renderer
                        _remoteVideoTrackStream.value = stream
                    }
                }
            }
            override fun onRemoveStream(p0: org.webrtc.MediaStream?) {}
            override fun onDataChannel(p0: org.webrtc.DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(p0: org.webrtc.RtpReceiver?, p1: Array<out org.webrtc.MediaStream>?) {}
        })

        currentCallConversationId = conversationId
        android.util.Log.d("ChatViewModel", "Calling webRtcClient.call()")
        webRtcClient?.call()
        android.util.Log.d("ChatViewModel", "startCall completed")
    }

    fun acceptCall(isVideo: Boolean = false) {
        if (webRtcClient == null && applicationContext != null) {
            initWebRtc(applicationContext!!)
        }

        _incomingCall.value = false
        _isInCall.value = true
        _isVideoCall.value = isVideo

        webRtcClient?.startLocalStream(isVideo = isVideo)

        webRtcClient?.createPeerConnection(object : org.webrtc.PeerConnection.Observer {
            override fun onSignalingChange(p0: org.webrtc.PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: org.webrtc.PeerConnection.IceConnectionState?) {
                p0?.let { Log.d("ChatViewModel", "❄️ (Callee) ICE Connection State Change: $it") }
            }
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: org.webrtc.PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(p0: org.webrtc.IceCandidate?) {
                p0?.let { candidate ->
                    val json = org.json.JSONObject().apply {
                        put("sdpMid", candidate.sdpMid)
                        put("sdpMLineIndex", candidate.sdpMLineIndex)
                        put("candidate", candidate.sdp)
                    }
                    if (callerSocketId != null) {
                        Log.d("ChatViewModel", "📤 (Callee) Sending ICE candidate")
                        socketManager?.emitIceCandidate(callerSocketId!!, json)
                    } else {
                        Log.e("ChatViewModel", "❌ (Callee) Cannot send ICE candidate: callerSocketId is null!")
                    }
                }
            }
            override fun onIceCandidatesRemoved(p0: Array<out org.webrtc.IceCandidate>?) {}
            override fun onAddStream(p0: org.webrtc.MediaStream?) {
                p0?.let { stream ->
                    Log.d("ChatViewModel", "🎥 (Callee) onAddStream: Remote stream received. Video tracks: ${stream.videoTracks.size}")
                    if (stream.videoTracks.isNotEmpty()) {
                        _remoteVideoTrackStream.value = stream
                    }
                }
            }
            override fun onRemoveStream(p0: org.webrtc.MediaStream?) {}
            override fun onDataChannel(p0: org.webrtc.DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(p0: org.webrtc.RtpReceiver?, p1: Array<out org.webrtc.MediaStream>?) {}
        })

        pendingOffer?.let { offerJson ->
            val sdp = org.webrtc.SessionDescription(
                org.webrtc.SessionDescription.Type.OFFER,
                offerJson.getString("sdp")
            )
            webRtcClient?.onRemoteSessionReceived(sdp)
            webRtcClient?.answer()
        }
    }

    fun declineCall() {
        _incomingCall.value = false
        currentCallConversationId?.let { cid ->
            socketManager?.emit("decline_call", org.json.JSONObject().put("conversationId", cid))
        }
        endCall()
    }

    fun endCall() {
        currentCallConversationId?.let { cid ->
            socketManager?.emitEndCall(cid)
        }
        webRtcClient?.close()
        _isInCall.value = false
        _incomingCall.value = false
        _remoteVideoTrackStream.value = null
        _isVideoCall.value = false

        // Reset mute states
        _isMicMuted.value = false
        _isVideoMuted.value = false
        _isSpeakerOn.value = false
    }

    fun attachLocalVideo(renderer: org.webrtc.SurfaceViewRenderer) {
        webRtcClient?.attachLocalVideo(renderer)
    }

    fun attachRemoteVideo(renderer: org.webrtc.SurfaceViewRenderer) {
        _remoteVideoTrackStream.value?.let { stream ->
            webRtcClient?.attachRemoteVideo(stream, renderer)
        }
    }

    // ===== Call Controls =====
    fun toggleMic() {
        val newState = !_isMicMuted.value
        _isMicMuted.value = newState
        webRtcClient?.toggleAudio(newState)
    }

    fun toggleVideo() {
        val newState = !_isVideoMuted.value
        _isVideoMuted.value = newState
        webRtcClient?.toggleVideo(newState)
    }

    fun switchCamera() {
        webRtcClient?.switchCamera()
    }

    fun toggleSpeaker(context: android.content.Context) {
        val newState = !_isSpeakerOn.value
        _isSpeakerOn.value = newState
        val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
        audioManager.isSpeakerphoneOn = newState
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