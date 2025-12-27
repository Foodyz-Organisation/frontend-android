package com.example.damprojectfinal.core.webrtc

import android.content.Context
import android.util.Log
import org.webrtc.*

class WebRtcClient(
    private val context: Context,
    private val eglBaseContext: EglBase.Context,
    private val onIceCandidate: (IceCandidate) -> Unit,
    private val onSessionDescription: (SessionDescription) -> Unit
) {
    private val TAG = "WebRtcClient"
    
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var videoSource: VideoSource? = null
    private var audioSource: AudioSource? = null
    
    private var isFrontCamera = true
    
    init {
        initializePeerConnectionFactory()
    }
    
    private fun initializePeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
        
        val encoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBaseContext)
        
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = false
                disableNetworkMonitor = false
            })
            .createPeerConnectionFactory()
        
        Log.d(TAG, "PeerConnectionFactory initialized")
    }
    
    fun createPeerConnection(observer: PeerConnection.Observer) {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
        )
        
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            keyType = PeerConnection.KeyType.ECDSA
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
        
        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, observer)
        
        // Add local tracks to peer connection
        localAudioTrack?.let { audio ->
            peerConnection?.addTrack(audio, listOf("local_stream"))
        }
        localVideoTrack?.let { video ->
            peerConnection?.addTrack(video, listOf("local_stream"))
        }
        
        Log.d(TAG, "PeerConnection created")
    }
    
    fun startLocalStream(isVideo: Boolean = false) {
        // Audio track
        audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory?.createAudioTrack("local_audio", audioSource)
        localAudioTrack?.setEnabled(true)
        
        if (isVideo) {
            // Video track
            val videoCapturer = createVideoCapturer()
            this.videoCapturer = videoCapturer
            
            if (videoCapturer != null) {
                videoSource = peerConnectionFactory?.createVideoSource(videoCapturer.isScreencast)
                localVideoTrack = peerConnectionFactory?.createVideoTrack("local_video", videoSource)
                localVideoTrack?.setEnabled(true)
                
                videoCapturer.initialize(
                    SurfaceTextureHelper.create("CaptureThread", eglBaseContext),
                    context,
                    videoSource?.capturerObserver
                )
                videoCapturer.startCapture(1280, 720, 30)
                
                Log.d(TAG, "Video capture started")
            }
        }
        
        Log.d(TAG, "Local stream started (video: $isVideo)")
    }
    
    private fun createVideoCapturer(): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
        
        // Try front camera first
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val capturer = enumerator.createCapturer(deviceName, null)
                if (capturer != null) {
                    isFrontCamera = true
                    return capturer
                }
            }
        }
        
        // Fallback to back camera
        for (deviceName in deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                val capturer = enumerator.createCapturer(deviceName, null)
                if (capturer != null) {
                    isFrontCamera = false
                    return capturer
                }
            }
        }
        
        return null
    }
    
    fun call() {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
        
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                sessionDescription?.let {
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onSetSuccess() {
                            Log.d(TAG, "Local description set successfully")
                            onSessionDescription(it)
                        }
                        override fun onCreateFailure(error: String?) {
                            Log.e(TAG, "Create failure: $error")
                        }
                        override fun onSetFailure(error: String?) {
                            Log.e(TAG, "Set local description failure: $error")
                        }
                    }, it)
                }
            }
            
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Create offer failure: $error")
            }
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }
    
    fun answer() {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
        
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                sessionDescription?.let {
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onSetSuccess() {
                            Log.d(TAG, "Local description set successfully")
                            onSessionDescription(it)
                        }
                        override fun onCreateFailure(error: String?) {}
                        override fun onSetFailure(error: String?) {
                            Log.e(TAG, "Set local description failure: $error")
                        }
                    }, it)
                }
            }
            
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Create answer failure: $error")
            }
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }
    
    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {
                Log.d(TAG, "Remote description set successfully")
            }
            override fun onCreateFailure(error: String?) {}
            override fun onSetFailure(error: String?) {
                Log.e(TAG, "Set remote description failure: $error")
            }
        }, sessionDescription)
    }
    
    fun addIceCandidate(iceCandidate: IceCandidate) {
        peerConnection?.addIceCandidate(iceCandidate)
        Log.d(TAG, "ICE candidate added")
    }
    
    fun switchCamera() {
        videoCapturer?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                this@WebRtcClient.isFrontCamera = isFrontCamera
                Log.d(TAG, "Camera switched to ${if (isFrontCamera) "front" else "back"}")
            }
            
            override fun onCameraSwitchError(errorDescription: String?) {
                Log.e(TAG, "Camera switch error: $errorDescription")
            }
        })
    }
    
    fun toggleAudio(mute: Boolean) {
        localAudioTrack?.setEnabled(!mute)
        Log.d(TAG, "Audio ${if (mute) "muted" else "unmuted"}")
    }
    
    fun toggleVideo(mute: Boolean) {
        localVideoTrack?.setEnabled(!mute)
        Log.d(TAG, "Video ${if (mute) "disabled" else "enabled"}")
    }
    
    fun attachLocalVideo(renderer: SurfaceViewRenderer) {
        renderer.init(eglBaseContext, null)
        renderer.setMirror(true)
        renderer.setEnableHardwareScaler(true)
        localVideoTrack?.addSink(renderer)
        Log.d(TAG, "Local video attached to renderer")
    }
    
    fun attachRemoteVideo(stream: MediaStream, renderer: SurfaceViewRenderer) {
        renderer.init(eglBaseContext, null)
        renderer.setMirror(false)
        renderer.setEnableHardwareScaler(true)
        
        if (stream.videoTracks.isNotEmpty()) {
            stream.videoTracks[0].addSink(renderer)
            Log.d(TAG, "Remote video attached to renderer")
        }
    }
    
    fun close() {
        try {
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            videoCapturer = null
            
            localVideoTrack?.dispose()
            localVideoTrack = null
            
            localAudioTrack?.dispose()
            localAudioTrack = null
            
            videoSource?.dispose()
            videoSource = null
            
            audioSource?.dispose()
            audioSource = null
            
            peerConnection?.close()
            peerConnection?.dispose()
            peerConnection = null
            
            Log.d(TAG, "WebRTC client closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing WebRTC client", e)
        }
    }
}
