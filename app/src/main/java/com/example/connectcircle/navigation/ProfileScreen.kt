package com.example.connectcircle.navigation

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.connectcircle.R
import com.example.connectcircle.RegistrationActivity
import com.example.connectcircle.UpdateProfileActivity
import com.example.connectcircle.models.UsersModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {

    var userData by remember { mutableStateOf<UsersModels?>(null) }

    val context = LocalContext.current

    LaunchedEffect(key1 = true) {

        val mFirestore = FirebaseFirestore.getInstance()
        mFirestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()


        mFirestore
            .collection("users")
            .document(mAuth.currentUser?.uid!!)
            .get()
            .addOnSuccessListener { document ->

                if (document != null && document.exists()) {
                    val user = document.toObject(UsersModels::class.java)
                    if (user != null) {

                        userData = UsersModels(
                            "",
                            user.fullName,
                            user.mobileNumber,
                            user.email,
                            user.areaOfInterest,
                            user.profilePicture,
                            true
                        )

                    } else {
                        Log.e("TAG", "ProfileScreen: User Not Found")
                    }
                } else {
                    Log.e("TAG", "ProfileScreen: User Not Found")
                }

            }.addOnFailureListener { e ->
                Log.e("TAG", "Error writing document", e)
            }
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
        modifier = Modifier
            .fillMaxSize()
    ) {
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(it),
            shape = RoundedCornerShape(topStart = 50.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                userData?.let { it1 -> ProfileHeader(user = it1) }
                Spacer(modifier = Modifier.height(16.dp))
                userData?.let { it1 -> ProfileDetails(user = it1) }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Edit Profile", color = Color.Green, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clickable {
                        context.startActivity(Intent(context, UpdateProfileActivity::class.java))
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Log Out", color = Color.Red, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ProfileHeader(user: UsersModels) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Surface(shape = RoundedCornerShape(100.dp)) {
            Image(
                rememberAsyncImagePainter(user.profilePicture),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(140.dp),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user.fullName,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = user.email,
            color = Color.Gray
        )
    }
}

@Composable
fun ProfileDetails(user: UsersModels) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            ProfileDetailItem(label = "Mobile Number", value = user.mobileNumber)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            ProfileDetailItem(label = "Area Of Interest", value = user.areaOfInterest)
        }
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
        )
    }
}

@Preview
@Composable
fun ProfilePreview() {
    ProfileScreen()
}

