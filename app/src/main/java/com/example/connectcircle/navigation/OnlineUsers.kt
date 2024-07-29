package com.example.connectcircle.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@Composable
fun OnlineUsers() {

    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Online Users")
    }

}

@Composable
fun ListUi(profilePicture: String, fullName: String, areaOfInterest: String, online: Any?) {

    Card(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    text = fullName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(text = areaOfInterest)

            }


            Text(
                text = if (online as Boolean) "Online" else "Offline",
                color = if (online as Boolean) Color.Green else Color.Red
            )

        }

    }

}
