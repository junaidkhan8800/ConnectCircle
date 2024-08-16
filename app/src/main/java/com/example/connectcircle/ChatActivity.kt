package com.example.connectcircle

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ChatActivity : ComponentActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConnectCircleTheme {

                val userId =
                    mAuth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
                val recipientId = intent.getStringExtra("recipientId")
                    ?: throw IllegalArgumentException("Recipient ID is missing")

                val fullName = intent.getStringExtra("fullName")
                    ?: throw IllegalArgumentException("Name is missing")

                val profilePicture = intent.getStringExtra("profilePicture")
                    ?: throw IllegalArgumentException("Profile Picture is missing")


                // Initialize ChatViewModel
                val chatViewModel: ChatViewModel = viewModel()

                // Set the recipient ID after initialization
                chatViewModel.setRecipientId(recipientId)
                chatViewModel.setFullName(fullName)
                chatViewModel.setProfilePicture(profilePicture)

                ChatAppUI(chatViewModel,recipientId)

            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChatAppUI(chatViewModel: ChatViewModel, recipientId: String) {

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

    //title = { Text(text = "Chat", color = Color.Black) },

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
                            // Handle call button click
                            context.startActivity(Intent(context, CallActivity::class.java).apply {
                                putExtra("target", recipientId)
                                putExtra("isVideoCall", false)
                                putExtra("isCaller", true)
                            })
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
                            // Handle video call button click
                            context.startActivity(Intent(context, CallActivity::class.java).apply {
                                putExtra("target", recipientId)
                                putExtra("isVideoCall", true)
                                putExtra("isCaller", true)
                            })
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
                        chatViewModel.addMessage()
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

    ChatAppUI(chatViewModel, "recipientId")

}