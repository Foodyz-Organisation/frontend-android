package com.example.damprojectfinal.core.webrtc

import android.content.Context
import android.util.Log
import org.json.JSONObject
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

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
    private var videoCapturer: VideoCapturer? = null
    
    // For audio only calls, we might not use video track
    private var isVideoCall = false

    init {
        initializePeerConnectionFactory()
    }

    private fun initializePeerConnectionFactory() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        val options = PeerConnectionFactory.Options()
        
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
            .createPeerConnectionFactory()
    }

    fun startLocalStream(isVideo: Boolean = false) {
        this.isVideoCall = isVideo
        val audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory?.createAudioTrack("ARDAMSa0", audioSource)
        
        if (isVideo) {
            videoCapturer = createVideoCapturer()
            videoCapturer?.let { capturer ->
                val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)
                val videoSource = peerConnectionFactory?.createVideoSource(capturer.isScreencast)
                capturer.initialize(surfaceTextureHelper, context, videoSource?.capturerObserver)
                capturer.startCapture(1280, 720, 30)
                
                localVideoTrack = peerConnectionFactory?.createVideoTrack("ARDAMSv0", videoSource)
            }
        }
    }
    
    private fun createVideoCapturer(): VideoCapturer? {
        val enumerator = if (Camera2Enumerator.isSupported(context)) {
            Camera2Enumerator(context)
        } else {
            Camera1Enumerator(true)
        }
        
        val deviceNames = enumerator.deviceNames
        
        // Try to find front facing camera
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        
        // Fallback to any camera
        for (deviceName in deviceNames) {
            val videoCapturer = enumerator.createCapturer(deviceName, null)
            if (videoCapturer != null) {
                return videoCapturer
            }
        }
        
        return null
    }
    
    fun attachLocalVideo(renderer: org.webrtc.SurfaceViewRenderer) {
        localVideoTrack?.addSink(renderer)
    }
    
    fun attachRemoteVideo(stream: MediaStream, renderer: org.webrtc.SurfaceViewRenderer) {
        if (stream.videoTracks.isNotEmpty()) {
            stream.videoTracks[0].addSink(renderer)
        }
    }

    fun createPeerConnection(observer: PeerConnection.Observer) {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA

        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, observer)
        
        localAudioTrack?.let {
            peerConnection?.addTrack(it, listOf("ARDAMS"))
        }
        localVideoTrack?.let {
            peerConnection?.addTrack(it, listOf("ARDAMS"))
        }
    }

    fun toggleAudio(shouldBeMuted: Boolean) {
        localAudioTrack?.setEnabled(!shouldBeMuted)
    }

    fun toggleVideo(shouldBeMuted: Boolean) {
        localVideoTrack?.setEnabled(!shouldBeMuted)
    }

    fun switchCamera() {
        (videoCapturer as? org.webrtc.CameraVideoCapturer)?.switchCamera(null)
    }

    fun call() {
        val constraints = MediaConstraints()
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", if(isVideoCall) "true" else "false"))

        peerConnection?.createOffer(object : SdpAdapter("createOffer") {
            override fun onCreateSuccess(desc: SessionDescription?) {
                super.onCreateSuccess(desc)
                peerConnection?.setLocalDescription(object : SdpAdapter("setLocalDescription") {
                    override fun onSetSuccess() {
                        super.onSetSuccess()
                        desc?.let { onSessionDescription(it) }
                    }
                }, desc)
            }
        }, constraints)
    }

    fun answer() {
        val constraints = MediaConstraints()
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", if(isVideoCall) "true" else "false"))

        peerConnection?.createAnswer(object : SdpAdapter("createAnswer") {
            override fun onCreateSuccess(desc: SessionDescription?) {
                super.onCreateSuccess(desc)
                peerConnection?.setLocalDescription(object : SdpAdapter("setLocalDescription") {
                    override fun onSetSuccess() {
                        super.onSetSuccess()
                        desc?.let { onSessionDescription(it) }
                    }
                }, desc)
            }
        }, constraints)
    }

    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SdpAdapter("setRemoteDescription") {}, sessionDescription)
    }

    fun addIceCandidate(iceCandidate: IceCandidate) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun close() {
        try {
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            localVideoTrack?.dispose()
            localAudioTrack?.dispose()
            peerConnection?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing WebRTC", e)
        }
    }

    // Helper Adapter to reduce boilerplate
    open class SdpAdapter(private val logTag: String) : org.webrtc.SdpObserver {
        override fun onCreateSuccess(desc: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(s: String?) { Log.e("SdpAdapter", "$logTag onCreateFailure: $s") }
        override fun onSetFailure(s: String?) { Log.e("SdpAdapter", "$logTag onSetFailure: $s") }
    }
}
