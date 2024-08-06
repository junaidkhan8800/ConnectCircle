package com.example.connectcircle.navigation

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.connectcircle.R
import com.example.connectcircle.models.UsersModels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineUsers(usersList: SnapshotStateList<UsersModels>, userDocumentId: String) {

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
        modifier = Modifier
            .fillMaxSize()

    ) { it ->

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            shape = RoundedCornerShape(topStart = 50.dp)
        ) {

            if (usersList.size != 0){

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {

                    items(usersList.size) { users ->
                        ListUi(
                            usersList[users].profilePicture,
                            usersList[users].fullName,
                            usersList[users].areaOfInterest,
                            usersList[users].isOnline
                        )
                    }
                }

            }else{

                Text(
                    text = "No Online Users",
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
fun ListUi(profilePicture: String, fullName: String, areaOfInterest: String, online: Any?) {

    Card(
        Modifier
            .padding(16.dp),
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
                color = if (online) Color.Green else Color.Red
            )

        }

    }

}
