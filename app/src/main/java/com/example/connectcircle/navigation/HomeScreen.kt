package com.example.connectcircle.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.connectcircle.R
import com.example.connectcircle.models.UsersModels
import com.example.connectcircle.utils.Constants.Companion.capitalizeWords
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {

    var search by remember { mutableStateOf("") }
    var listVisible by remember { mutableStateOf(false) }
//
//// Use mutableStateListOf to track changes in the list
    val usersList = remember { mutableStateListOf<UsersModels>() }

    val context = LocalContext.current

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

            Column(modifier = Modifier
                .fillMaxSize()) {

                OutlinedTextField(
                    value = search,
                    onValueChange = {
                        search = it
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {

                            val mFirestore = FirebaseFirestore.getInstance()
                            mFirestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()


                            mFirestore
                                .collection("users")
                                .whereEqualTo("areaOfInterest", search.capitalizeWords().trim())
                                .get()
                                .addOnSuccessListener { documents ->
                                    try {
                                        if (documents != null) {
                                            for (document in documents) {
                                                Log.d("TAG", "${document.id} => ${document.data}")

                                                usersList.clear()

                                                usersList.add(
                                                    UsersModels(
                                                        document.id,
                                                        document.get("fullName").toString(),
                                                        document.get("mobileNumber").toString(),
                                                        document.get("email").toString(),
                                                        document.get("areaOfInterest").toString(),
                                                        document.get("profilePicture").toString(),
                                                        document.get("isOnline")
                                                    )
                                                )
                                            }

                                            listVisible = usersList.isNotEmpty()

//                    Toast.makeText(context, "DocumentSnapshot read successfully!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "No such Users", Toast.LENGTH_LONG)
                                                .show()
                                        }
                                    } catch (ex: Exception) {
                                        ex.message?.let { Log.e("TAG", it) }
                                    }
                                }.addOnFailureListener { e ->
                                    Log.e("TAG", "Error writing document", e)
                                }

                        }
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    maxLines = 1,
                    label = { Text(text = "Search") }
                )

                AnimatedVisibility(visible = !listVisible) {
                    Text(
                        text = "Search for the people with similar interest like you.",
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                AnimatedVisibility(visible = listVisible) {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {

                        items(usersList.size) { users ->
                            ListUI(
                                usersList[users].profilePicture,
                                usersList[users].fullName,
                                usersList[users].areaOfInterest,
                                usersList[users].isOnline
                            )
                        }
                    }
                }

            }

        }
    }

}


@Composable
fun ListUI(profilePicture: String, fullName: String, areaOfInterest: String, online: Any?) {

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

@Preview
@Composable
fun HomePrev() {
    HomeScreen()
}