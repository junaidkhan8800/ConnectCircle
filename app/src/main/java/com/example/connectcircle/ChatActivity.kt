package com.example.connectcircle

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.connectcircle.ui.theme.ConnectCircleTheme
import com.example.connectcircle.utils.Constants
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.jitsi.meet.sdk.JitsiMeet
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONObject
import java.io.IOException
import java.net.URL


class ChatActivity : ComponentActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConnectCircleTheme {

                val userId =
                    intent.getStringExtra("senderName") ?: throw IllegalStateException("Sender Name is missing")

                val recipientId = intent.getStringExtra("recipientId")
                    ?: throw IllegalArgumentException("Recipient ID is missing")

                val fullName = intent.getStringExtra("fullName")
                    ?: throw IllegalArgumentException("Name is missing")

                val profilePicture = intent.getStringExtra("profilePicture")
                    ?: throw IllegalArgumentException("Profile Picture is missing")

                val fcmToken = intent.getStringExtra("fcmToken")
                    ?: throw IllegalArgumentException("FCMToken is missing")


                // Initialize ChatViewModel
                val chatViewModel: ChatViewModel = viewModel()

                // Set the recipient ID after initialization
                chatViewModel.setRecipientId(recipientId)
                chatViewModel.setFullName(fullName)
                chatViewModel.setProfilePicture(profilePicture)

                ChatAppUI(chatViewModel, recipientId, fcmToken,userId)

            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChatAppUI(chatViewModel: ChatViewModel, recipientId: String, fcmToken: String, userId: String) {

    val message: String by chatViewModel.message.observeAsState("")
    val messages: List<Map<String, Any>> by chatViewModel.messages.observeAsState(emptyList())

    val fullName: String by chatViewModel.fullName.observeAsState("")
    val profilePicture: String by chatViewModel.profilePicture.observeAsState("")

    val context = LocalContext.current

    // Define permissions to request
    val cameraPermission = android.Manifest.permission.CAMERA
    val recordAudioPermission = android.Manifest.permission.RECORD_AUDIO

    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(cameraPermission, recordAudioPermission)
    )

    val serverUrl = URL("https://jitsi.unp.edu.ar")

    try {

        val defaultOptions = JitsiMeetConferenceOptions.Builder()
            .setServerURL(serverUrl)
            .setFeatureFlag("welcomepage.enabled", false)
            .build()

        JitsiMeet.setDefaultConferenceOptions(defaultOptions)


    } catch (e: Exception) {
        e.printStackTrace()
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(100.dp)) {
                            Image(
                                rememberAsyncImagePainter(profilePicture),
                                contentDescription = "Profile Image",
                                modifier = Modifier.size(50.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text(
                            text = fullName, modifier = Modifier.padding(8.dp),
                            color = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = {

                        multiplePermissionsState.launchMultiplePermissionRequest()
                        if (multiplePermissionsState.allPermissionsGranted) {


//                            .setAudioOnly(false)
                        }

                    }) {
                        Icon(
                            tint = Color.Black,
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Call"
                        )
                    }
                    IconButton(onClick = {

                        multiplePermissionsState.launchMultiplePermissionRequest()
                        if (multiplePermissionsState.allPermissionsGranted) {

                            val options = JitsiMeetConferenceOptions.Builder()
                                .setRoom("${Firebase.auth.currentUser?.uid}/$recipientId")
                                .setFeatureFlag("invite.enabled", false)
                                .setFeatureFlag("lobby-mode.enabled", false)
                                .setFeatureFlag("prejoinpage.enabled", false)
//                                .setFeatureFlag("tile-view.enabled",true)
                                .setFeatureFlag("welcomepage.enabled", false)
                                .build()

                            JitsiMeetActivity.launch(context, options)

                            CoroutineScope(Dispatchers.IO).launch {
                                sendCallNotification(recipientId, fcmToken, context)
                            }

                        }

                    }) {
                        Icon(
                            tint = Color.Black,
                            imageVector = Icons.Filled.VideoCall,
                            contentDescription = "Video Call"
                        )
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                reverseLayout = true
            ) {
                items(messages) { message ->
                    val isCurrentUser = message[Constants.IS_CURRENT_USER] as Boolean

                    SingleMessage(
                        message = message[Constants.MESSAGE].toString(),
                        isCurrentUser = isCurrentUser
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = {
                        chatViewModel.updateMessage(it)
                    },
                    label = {
                        Text("Type Your Message")
                    },
                    shape = RoundedCornerShape(25.dp),
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    ),
                    singleLine = true,
                )
                IconButton(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 8.dp)
                        .size(58.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    onClick = {

                        CoroutineScope(Dispatchers.Default).launch {
                            chatViewModel.addMessage()
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            sendChatNotification(fcmToken, message, userId, context)
                        }
                    }
                ) {
                    Icon(
                        tint = Color.Black,
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Button"
                    )
                }
            }
        }
    }
}

fun sendChatNotification(fcmToken: String, message: String, fullName: String, context: Context) {

    if (fullName.isEmpty()) {
        Log.w("ChatNotification", "Full Name is empty")
        return
    }

    // Construct the notification payload
    val payload = JSONObject().apply {
        put("message", JSONObject().apply {
            put("token", fcmToken)
            put("data", JSONObject().apply {
                put("type", "chat_message")
                put("senderName", fullName)
                put("message", message)
            })
            put("android", JSONObject().apply {
                put("priority", "high")
            })
        })
    }

    // Send the notification
    val client = OkHttpClient()
    val requestBody = RequestBody.create(
        "application/json; charset=utf-8".toMediaTypeOrNull(),
        payload.toString()
    )
    val request = Request.Builder()
        .url("https://fcm.googleapis.com/v1/projects/connect-circle-23dca/messages:send")  // Update with your FCM URL
        .post(requestBody)
        .addHeader("Authorization", "Bearer " + getServiceAccountAccessToken(context))  // Replace with your server key
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            Log.e("ChatNotification", "Failed to send notification: ", e)
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            if (!response.isSuccessful) {
                Log.e("ChatNotification", "Error sending notification: ${response.message}")
            } else {
                Log.d("ChatNotification", "Notification sent successfully.")
            }
        }
    })

}

fun sendCallNotification(recipientId: String, fcmToken: String, context: Context) {

    if (fcmToken.isEmpty()) {
        Log.w("CallNotification", "FCM token is empty")
        return
    }

    // Construct the notification payload
    val payload = JSONObject().apply {
        put("message", JSONObject().apply {
            put("token", fcmToken)
            put("data", JSONObject().apply {
                put("type", "video_call")
                put("callerId", FirebaseAuth.getInstance().currentUser?.uid ?: "")
                put("callId", "${Firebase.auth.currentUser?.uid}/$recipientId")  // Unique identifier for the call
            })
            put("android", JSONObject().apply {
                put("priority", "high")
            })
        })
    }

    // Send the notification
    val client = OkHttpClient()
    val requestBody = RequestBody.create(
        "application/json; charset=utf-8".toMediaTypeOrNull(),
        payload.toString()
    )
    val request = Request.Builder()
        .url("https://fcm.googleapis.com/v1/projects/connect-circle-23dca/messages:send") //connect-circle-23dca
        .post(requestBody)
        .addHeader("Authorization", "Bearer " + getServiceAccountAccessToken(context))  // Replace with your server key
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            Log.e("CallNotification", "Failed to send notification: ", e)
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            if (!response.isSuccessful) {
                Log.e("CallNotification", "Error sending notification: ${response.message}")
            } else {
                Log.d("CallNotification", "Notification sent successfully.")
            }
        }
    })

}

@Throws(IOException::class)
fun getServiceAccountAccessToken(context: Context): String {

    val serviceAccount = context.resources.openRawResource(R.raw.service_account_key)

    val credentials = GoogleCredentials.fromStream(serviceAccount)
        .createScoped("https://www.googleapis.com/auth/firebase.messaging")

    credentials.refresh()
    return credentials.accessToken.tokenValue

}


@Composable
fun SingleMessage(message: String, isCurrentUser: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = if (isCurrentUser) Modifier.align(Alignment.CenterEnd) else Modifier.align(
                Alignment.CenterStart
            ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.inversePrimary
            )
        ) {
            Text(
                text = message,
                textAlign =
                if (isCurrentUser)
                    TextAlign.End
                else
                    TextAlign.Start,
                modifier = Modifier
                    .padding(16.dp),
//                color = if (!isCurrentUser) MaterialTheme.colorScheme.primary else Color.White
//                color = Color.Black
            )
        }
    }
}

@Preview
@Composable
private fun ChatPrev() {

    // Initialize ChatViewModel
    val chatViewModel: ChatViewModel = viewModel()

    ChatAppUI(chatViewModel, "recipientId", "fcmToken", "userId")

}