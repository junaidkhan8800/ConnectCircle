package com.example.connectcircle

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import org.webrtc.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CallActivity : ComponentActivity() {

    private val callViewModel: CallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val recipientId = intent.getStringExtra("target")
                ?: throw IllegalArgumentException("Recipient ID is missing")

            val isCaller = intent.getBooleanExtra("isCaller", true)

            CallScreen(
                callViewModel = callViewModel,
                recipientId = recipientId,
                isCaller = isCaller
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CallScreen(callViewModel: CallViewModel, recipientId: String, isCaller: Boolean) {
    val context = LocalContext.current
    val permissionsState = rememberMultiplePermissionsState(
        listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    )

    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
        if (permissionsState.allPermissionsGranted) {
            callViewModel.initialize(context)
            callViewModel.setupCall(recipientId, isCaller)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { SurfaceViewRenderer(context) },
                update = { surfaceView ->
                    callViewModel.localVideoView = surfaceView
                    callViewModel.initializeSurfaceView(surfaceView)
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            AndroidView(
                factory = { SurfaceViewRenderer(context) },
                update = { surfaceView ->
                    callViewModel.remoteVideoView = surfaceView
                    callViewModel.initializeSurfaceView(surfaceView)
                },
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = { callViewModel.endCall() },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text(text = "End Call")
        }
    }
}


class CallViewModel : ViewModel() {
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var peerConnection: PeerConnection
    private lateinit var videoCapturer: VideoCapturer
    private lateinit var localVideoTrack: VideoTrack
    private lateinit var remoteVideoTrack: VideoTrack
    var localVideoView: SurfaceViewRenderer? = null
    var remoteVideoView: SurfaceViewRenderer? = null
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var callRef: DatabaseReference

    fun initialize(context: Context) {
        firebaseDatabase = FirebaseDatabase.getInstance()
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .createInitializationOptions()
        )
        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()
    }

    fun setupCall(recipientId: String, isCaller: Boolean) {
        callRef = firebaseDatabase.getReference("calls").child(recipientId)
        initializePeerConnections()
        startVideoCapture()
        if (isCaller) {
            startCall()
        } else {
            listenForRemoteOffer()
        }
        listenForIceCandidates()
    }

    fun initializeSurfaceView(surfaceView: SurfaceViewRenderer) {
        surfaceView.init(EglBase.create().eglBaseContext, null)
        surfaceView.setEnableHardwareScaler(true)
    }

    private fun initializePeerConnections() {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
        peerConnection = peerConnectionFactory.createPeerConnection(
            iceServers,
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate?) {
                    if (candidate != null) {
                        Log.d("WebRTC", "ICE Candidate gathered: ${candidate.sdp}")
                        callRef.child("candidates").push().setValue(candidate)
                    }
                }

                override fun onAddStream(stream: MediaStream?) {
                    if (stream != null) {
                        remoteVideoTrack = stream.videoTracks.first()
                        remoteVideoTrack.addSink(remoteVideoView)
                    }
                }

                override fun onSignalingChange(state: PeerConnection.SignalingState?) {
                    Log.d("WebRTC", "Signaling State: $state")
                }

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                    Log.d("WebRTC", "ICE Connection State: $state")
                }

                override fun onIceConnectionReceivingChange(p0: Boolean) {}
                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {
                    Log.d("WebRTC", "ICE Gathering State: $state")
                }

                override fun onDataChannel(channel: DataChannel?) {}
                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
                override fun onRemoveStream(stream: MediaStream?) {}
                override fun onRenegotiationNeeded() {}
                override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
            }
        )!!
    }

    private fun startVideoCapture() {
        videoCapturer = createCameraCapturer(Camera1Enumerator(false)) ?: return
        val surfaceTextureHelper =
            SurfaceTextureHelper.create("CaptureThread", EglBase.create().eglBaseContext)
        val videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast)
        videoCapturer.initialize(surfaceTextureHelper, localVideoView?.context, videoSource.capturerObserver)

        localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource)
        localVideoTrack.addSink(localVideoView)

        val mediaStream = peerConnectionFactory.createLocalMediaStream("mediaStream")
        mediaStream.addTrack(localVideoTrack)
        peerConnection.addStream(mediaStream)

        Log.d("WebRTC", "Video capture started. LocalVideoTrack: $localVideoTrack, MediaStream: $mediaStream")
    }

    private fun startCall() {
        peerConnection.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                peerConnection.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {
                        Log.d("WebRTC", "Local description set successfully")
                        callRef.child("offer").setValue(sdp)
                    }
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {
                        Log.e("WebRTC", "Failed to set local description: $p0")
                    }
                }, sdp)
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.e("WebRTC", "Failed to create offer: $error")
            }
            override fun onSetFailure(error: String?) {}
        }, createMediaConstraints())
    }

    private fun listenForRemoteOffer() {
        callRef.child("offer").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offer = snapshot.getValue(SessionDescription::class.java)
                if (offer != null) {
                    peerConnection.setRemoteDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onSetSuccess() {
                            Log.d("WebRTC", "Remote offer set successfully")
                            answerCall()
                        }
                        override fun onCreateFailure(p0: String?) {}
                        override fun onSetFailure(p0: String?) {
                            Log.e("WebRTC", "Failed to set remote description: $p0")
                        }
                    }, offer)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun answerCall() {
        peerConnection.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                peerConnection.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {
                        Log.d("WebRTC", "Local description set successfully")
                        callRef.child("answer").setValue(sdp)
                    }
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {
                        Log.e("WebRTC", "Failed to set local description: $p0")
                    }
                }, sdp)
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.e("WebRTC", "Failed to create answer: $error")
            }
            override fun onSetFailure(error: String?) {}
        }, createMediaConstraints())
    }

    private fun createMediaConstraints(): MediaConstraints {
        val constraints = MediaConstraints()
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        return constraints
    }

    private fun listenForIceCandidates() {
        callRef.child("candidates").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val candidate = snapshot.getValue(IceCandidate::class.java)
                if (candidate != null) {
                    peerConnection.addIceCandidate(candidate)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }
        return null
    }

    fun endCall() {
        peerConnection.close()
        localVideoView?.release()
        remoteVideoView?.release()
        videoCapturer.stopCapture()
    }
}
