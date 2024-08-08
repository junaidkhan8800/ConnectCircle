package com.example.connectcircle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.connectcircle.ui.theme.ConnectCircleTheme



class ChatScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConnectCircleTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    ChatScreen()

                }

            }
        }

    }

}

@Composable
fun ChatScreen() {
    var recipientId by remember { mutableStateOf(TextFieldValue()) }
    var messageText by remember { mutableStateOf(TextFieldValue()) }
    val messageList = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Recipient input
        TextField(
            value = recipientId,
            onValueChange = { recipientId = it },
            placeholder = { Text("Enter recipient user ID") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .background(Color.White)
        )

        // Message list
        Column(
            modifier = Modifier
                .fillMaxSize().weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            messageList.forEach { message ->
                Text(text = message)
            }
        }

        // Message input
        TextField(
            value = messageText,
            onValueChange = { messageText = it },
            placeholder = { Text("Message") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .background(Color.White)
        )

        // Send button
        Button(
            onClick = {
                // Handle sending the message
                messageList.add(messageText.text)
                messageText = TextFieldValue("")
            },
            modifier = Modifier
                .align(Alignment.End)
                .padding(5.dp)
        ) {
            Text("Send")
        }
    }
}

@Preview
@Composable
private fun ChatUI() {
    ChatScreen()
}
