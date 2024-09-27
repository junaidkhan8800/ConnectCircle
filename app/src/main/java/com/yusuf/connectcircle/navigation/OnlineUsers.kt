package com.yusuf.connectcircle.navigation

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.yusuf.connectcircle.ChatActivity
import com.yusuf.connectcircle.PastChatsViewModel
import com.yusuf.connectcircle.R
import com.yusuf.connectcircle.models.UsersModels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastChatsScreen(
    userDocumentId: String,  // Current user's document ID
    userData: UsersModels,   // Current user data
    pastChatsViewModel: PastChatsViewModel = viewModel()  // Use the separate ViewModel
) {
    val context = LocalContext.current

    // Observe past chats from ViewModel
    val pastChats by pastChatsViewModel.pastChats.observeAsState(initial = emptyList())

    // Fetch past chats only once when the screen loads
    LaunchedEffect(Unit) {
        pastChatsViewModel.fetchPastChats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        color = Color.Black
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                windowInsets = WindowInsets(
                    top = dimensionResource(id = R.dimen.size_0dp),
                    bottom = dimensionResource(id = R.dimen.size_0dp)
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            shape = RoundedCornerShape(topStart = 50.dp)
        ) {

            if (pastChats.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(pastChats.size) { index ->
                        val user = pastChats[index]
                        ListUi(
                            context = context,
                            userId = user.recipientId,
                            profilePicture = user.profilePicture,
                            fullName = user.fullName,
                            areaOfInterest = user.areaOfInterest,
                            online = user.isOnline,
                            fcmToken = user.fcmToken,
                            userData = userData,
                            messageFrom = user.messageFrom,
                            lastMessage = user.lastMessage,
                            lastMessageTimestamp = user.lastMessageTimestamp
                        )
                    }
                }
            } else {
                Text(
                    text = "No Chats",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxSize(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}



@Composable
fun ListUi(
    context: Context,
    userId: String,
    profilePicture: String,
    fullName: String,
    areaOfInterest: String,
    online: Any?,
    fcmToken: String,
    userData: UsersModels,
    messageFrom: String,
    lastMessage: String,  // New: Last message to display
    lastMessageTimestamp: Long  // New: Timestamp of the last message
) {

    Card(
        Modifier
            .padding(16.dp)
            .clickable {
                // Open ChatActivity when the user is clicked
                val intent = Intent(context, ChatActivity::class.java)

                intent.putExtra("recipientId", userId)
                intent.putExtra("fullName", messageFrom)
                intent.putExtra("profilePicture", profilePicture)
                intent.putExtra("fcmToken", fcmToken)
                intent.putExtra("senderName", userData.fullName)

                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(shape = RoundedCornerShape(100.dp)) {
                Image(
                    rememberAsyncImagePainter(profilePicture),
                    contentDescription = "Profile Image",
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                Modifier
                    .padding(16.dp)
                    .weight(1F)
            ) {

                Text(
                    text = messageFrom,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(text = lastMessage)  // Display the last message

                // Convert timestamp to readable format (optional)
                Text(
                    text = "Last message: ${android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", lastMessageTimestamp)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

            }

            // Online status
//            Text(
//                text = if (online as Boolean) "Online" else "Offline",
//                color = if (online as Boolean) Color.Green else Color.Red
//            )

        }

    }

}
