package com.example.chatapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.agora.CallBack
import io.agora.ConnectionListener
import io.agora.chat.ChatClient
import io.agora.chat.ChatMessage
import io.agora.chat.ChatOptions
import io.agora.chat.TextMessageBody


class ChatScreenActivity : ComponentActivity() {
    private lateinit var agoraChatClient: ChatClient
    private var isJoined by mutableStateOf(false)
    private var userId by mutableStateOf("YourUserID")
    private var token by mutableStateOf("YourAuthToken")
    private var appKey by mutableStateOf("YourAppKey")

    private var recipientId by mutableStateOf("")
    private var messageText by mutableStateOf("")
    private var messages by mutableStateOf(listOf<String>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val intent = intent

            userId = intent.getStringExtra("userId").toString()
            recipientId = intent.getStringExtra("recipientId").toString()

            ChatAppUI(
                isJoined = isJoined,
                recipientId = recipientId,
                messageText = messageText,
                messages = messages,
                onRecipientIdChange = { recipientId = it },
                onMessageTextChange = { messageText = it },
                onJoinLeaveClick = { joinLeave() },
                onSendMessageClick = { sendMessage() },
                onMessageReceived = { newMessage -> messages = messages + newMessage }
            )
        }
        setupChatClient()
        setupListeners()
    }

    private fun setupChatClient() {
        val options = ChatOptions().apply { setAppKey(appKey) }
        agoraChatClient = ChatClient.getInstance().apply {
            init(this@ChatScreenActivity, options)
            setDebugMode(true)
        }
    }

    private fun setupListeners() {
        agoraChatClient.chatManager().addMessageListener { messages ->
            messages.forEach { message ->
                if (message.body is TextMessageBody) {
                    val text = (message.body as TextMessageBody).message
                    runOnUiThread { displayMessage(text, false) }
                }
            }
        }

        agoraChatClient.addConnectionListener(object : ConnectionListener {
            override fun onConnected() {
                showToast("Connected")
            }

            override fun onDisconnected(error: Int) {
                if (isJoined) {
                    showToast("Disconnected: $error")
                    isJoined = false
                }
            }

            override fun onLogout(errorCode: Int) {
                showToast("User logging out: $errorCode")
            }

            override fun onTokenExpired() {
                // Handle token expiration
            }

            override fun onTokenWillExpire() {
                // Handle token expiration warning
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun joinLeave() {
        if (isJoined) {
            agoraChatClient.logout(true, object : CallBack {
                override fun onSuccess() {
                    showToast("Sign out success!")
                    isJoined = false
                }

                override fun onError(code: Int, error: String) {
                    showToast(error)
                }
            })
        } else {
            agoraChatClient.loginWithAgoraToken(userId, token, object : CallBack {
                override fun onSuccess() {
                    showToast("Signed in")
                    isJoined = true
                }

                override fun onError(code: Int, error: String) {
                    if (code == 200) { // Already joined
                        isJoined = true
                    } else {
                        showToast(error)
                    }
                }
            })
        }
    }

    private fun sendMessage() {
        if (recipientId.isBlank() || messageText.isBlank()) {
            showToast("Enter a recipient name and a message")
            return
        }

        val message = ChatMessage.createTextSendMessage(messageText, recipientId)
        message.setMessageStatusCallback(object : CallBack {
            override fun onSuccess() {
                showToast("Message sent")
                displayMessage(messageText, true)
                messageText = ""
            }

            override fun onError(code: Int, error: String) {
                showToast(error)
            }
        })
        agoraChatClient.chatManager().sendMessage(message)
    }

    private fun displayMessage(messageText: String, isSentMessage: Boolean) {
        runOnUiThread {
            messages = messages + messageText
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAppUI(
    isJoined: Boolean,
    recipientId: String,
    messageText: String,
    messages: List<String>,
    onRecipientIdChange: (String) -> Unit,
    onMessageTextChange: (String) -> Unit,
    onJoinLeaveClick: () -> Unit,
    onSendMessageClick: () -> Unit,
    onMessageReceived: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat App") }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                Button(onClick = onJoinLeaveClick) {
                    Text(if (isJoined) "Leave" else "Join")
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = recipientId,
                    onValueChange = onRecipientIdChange,
                    label = { Text("Recipient ID") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Gray)
                        .padding(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = messageText,
                    onValueChange = onMessageTextChange,
                    label = { Text("Message") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Gray)
                        .padding(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onSendMessageClick) {
                    Text("Send")
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(messages) { message ->
                        Text(message, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    )
}


@Preview
@Composable
private fun ChatPrev() {

    lateinit var agoraChatClient: ChatClient
    var isJoined by remember { mutableStateOf(false) }
    var userId by remember { mutableStateOf("YourUserID") }
    var token by remember { mutableStateOf("YourAuthToken") }
    var appKey by remember { mutableStateOf("YourAppKey") }
    var recipientId by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<String>()) }

    ChatAppUI(
        isJoined = isJoined,
        recipientId = recipientId,
        messageText = messageText,
        messages = messages,
        onRecipientIdChange = { recipientId = it },
        onMessageTextChange = { messageText = it },
        onJoinLeaveClick = {  },
        onSendMessageClick = {  },
        onMessageReceived = { newMessage -> messages = messages + newMessage }
    )

}