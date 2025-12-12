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
import com.example.damprojectfinal.core.api.UserDto
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
    
    // ===== WebRTC =====
    private var webRtcClient: com.example.damprojectfinal.core.webrtc.WebRtcClient? = null
    private val _incomingCall = MutableStateFlow<Boolean>(false)
    val incomingCall: StateFlow<Boolean> = _incomingCall
    
    private val _isInCall = MutableStateFlow<Boolean>(false)
    val isInCall: StateFlow<Boolean> = _isInCall
    
    // Context needed for WebRTC initialization - passed via initWebRtc
    private var applicationContext: android.content.Context? = null
    private var eglBaseContext: org.webrtc.EglBase.Context? = null

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
                // Use getChats to get pre-resolved names and avatars
                val response = chatApiService.getChats("Bearer $authToken")

                // Mapping DTO -> modèle UI
                _chatItems.value = response.map { summary ->
                    ChatListItem(
                        id = summary.id,
                        title = summary.name,
                        subtitle = summary.message,
                        updatedTime = formatTimestamp(summary.time),
                        unreadCount = summary.unreadCount,
                        isOnline = summary.online,
                        avatarUrl = summary.avatarUrl,
                        initials = initialsFrom(summary.name)
                    )
                }
                
                // Also fetch full conversations for internal logic if needed (e.g. participants for calls)
                // But for the list, we are good.
                // We might want to fetch conversations in background to populate 'conversations' list for 'onUserSelected' logic
                try {
                    val convs = chatApiService.getConversations("Bearer $authToken")
                    conversations = convs.filter { it.id != null }
                } catch (e: Exception) {
                    // Ignore error here, main list is already loaded
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

    // ===== User Search =====
    private val _searchResults = MutableStateFlow<List<UserDto>>(emptyList())
    val searchResults: StateFlow<List<UserDto>> = _searchResults

    fun searchUsers(authToken: String, query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
                return@launch
            }
            try {
                val results = chatApiService.searchUsers("Bearer $authToken", query)
                _searchResults.value = results
            } catch (e: Exception) {
                // Handle error silently or show toast
                _searchResults.value = emptyList()
            }
        }
    }

    fun onUserSelected(
        authToken: String, 
        currentUserId: String, 
        selectedUser: UserDto, 
        onConversationReady: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isStartingConversation.value = true
            try {
                // 1. Check if conversation exists
                val existing = conversations.find { conv ->
                    conv.kind == "private" && 
                    conv.participants.contains(selectedUser.id) && 
                    conv.participants.contains(currentUserId)
                }

                if (existing != null && existing.id != null) {
                    onConversationReady(existing.id)
                } else {
                    // 2. Create new conversation
                    val dto = CreateConversationDto(
                        kind = "private",
                        participants = listOf(selectedUser.id)
                    )
                    val newConv = chatApiService.createConversation("Bearer $authToken", dto)
                    if (newConv.id != null) {
                        onConversationReady(newConv.id)
                        // Refresh list
                        loadConversations(authToken, currentUserId, false)
                    }
                }
            } catch (e: Exception) {
                _startConversationError.value = "Failed to start chat: ${e.message}"
            } finally {
                _isStartingConversation.value = false
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

    fun disconnectSocket() {
        socketManager?.disconnect()
        _isSocketConnected.value = false
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
    //        WebRTC
    // =======================
    
    private var pendingOffer: org.json.JSONObject? = null
    private var callerSocketId: String? = null
    
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
    
    private val _isVideoCall = MutableStateFlow(false)
    val isVideoCall: StateFlow<Boolean> = _isVideoCall

    fun startCall(conversationId: String, isVideo: Boolean = false) {
        if (webRtcClient == null && applicationContext != null) {
            initWebRtc(applicationContext!!)
        }
        
        _isInCall.value = true
        _isVideoCall.value = isVideo
        webRtcClient?.startLocalStream(isVideo = isVideo)
        
        webRtcClient?.createPeerConnection(object : org.webrtc.PeerConnection.Observer {
            override fun onSignalingChange(p0: org.webrtc.PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: org.webrtc.PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: org.webrtc.PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(p0: org.webrtc.IceCandidate?) {}
            override fun onIceCandidatesRemoved(p0: Array<out org.webrtc.IceCandidate>?) {}
            override fun onAddStream(p0: org.webrtc.MediaStream?) {
                // Handle remote stream
                p0?.let { stream ->
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
        webRtcClient?.call()
    }
    
    private var currentCallConversationId: String? = null
    
    // We need to update initWebRtc to use currentCallConversationId

    
    // Helper to actually send the offer since the callback is generic
    fun sendOffer(conversationId: String, sdp: org.webrtc.SessionDescription) {
        val json = org.json.JSONObject().apply {
            put("type", sdp.type.canonicalForm())
            put("sdp", sdp.description)
        }
        socketManager?.emitCallUser(conversationId, json)
    }

    private val _remoteVideoTrackStream = MutableStateFlow<org.webrtc.MediaStream?>(null)
    val remoteVideoTrackStream: StateFlow<org.webrtc.MediaStream?> = _remoteVideoTrackStream

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
            override fun onIceConnectionChange(p0: org.webrtc.PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: org.webrtc.PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(p0: org.webrtc.IceCandidate?) {}
            override fun onIceCandidatesRemoved(p0: Array<out org.webrtc.IceCandidate>?) {}
            override fun onAddStream(p0: org.webrtc.MediaStream?) {
                 p0?.let { stream ->
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
    private val _isMicMuted = MutableStateFlow(false)
    val isMicMuted: StateFlow<Boolean> = _isMicMuted
    
    private val _isVideoMuted = MutableStateFlow(false)
    val isVideoMuted: StateFlow<Boolean> = _isVideoMuted
    
    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn
    
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