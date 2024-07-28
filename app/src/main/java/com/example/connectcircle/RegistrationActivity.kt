package com.example.connectcircle

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.connectcircle.ui.theme.ConnectCircleTheme
import com.example.connectcircle.utils.Constants.Companion.capitalizeWords
import com.example.connectcircle.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class RegistrationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConnectCircleTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    RegistrationUI()

                }

            }
        }

    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationUI() {


    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val userId = mAuth.currentUser?.uid
    val profileImagePath = "UserImages/$userId/profile_picture.jpg"
    var storageReference: StorageReference =
        FirebaseStorage.getInstance().reference.child(profileImagePath)

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            selectedImageUri = it
        })

    var name by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var areaOfInterest by remember { mutableStateOf("") }

    val maxNumberCount = 10

    val context = LocalContext.current

    val preferencesManager = remember { PreferencesManager(context) }

    val number = preferencesManager.getMobileNumber("mobileNumber", "")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Personal Details") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxSize()

    ) { it ->
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            shape = RoundedCornerShape(topStart = 50.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Surface(shape = RoundedCornerShape(100.dp)) {

                        AsyncImage(
                            modifier = Modifier.size(150.dp),
                            model = selectedImageUri,
                            contentDescription = "Profile Image",
                            contentScale = ContentScale.Crop
                        )

//                        Image(
//                            painter = painterResource(id = R.drawable.ic_launcher_background),
//                            contentDescription = "Profile Image",
//                            modifier = Modifier.size(150.dp)
//                        )
                    }
                    Box(
                        Modifier
                            .shadow(shape = RoundedCornerShape(50.dp), elevation = 5.dp)
                            .background(color = Color.White)
                            .align(Alignment.BottomEnd)
                            .clickable(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit, contentDescription = "Edit Image",
                            modifier = Modifier
                                .size(50.dp)
                                .padding(8.dp)
                        )
                    }
                }

                OutlinedTextField(value = name,
                    onValueChange = {
                        name = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    maxLines = 1,
                    label = { Text(text = "Full Name") })



                OutlinedTextField(value = number!!,
                    onValueChange = {
                        if (it.length <= maxNumberCount) {
                            mobileNumber = it
                        }
                    },
                    enabled = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    maxLines = 1,
                    label = { Text(text = "Mobile Number") })

                OutlinedTextField(value = email,
                    onValueChange = {
                        email = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    maxLines = 1,
                    label = { Text(text = "Email") })

                OutlinedTextField(value = areaOfInterest,
                    onValueChange = {
                        areaOfInterest = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    maxLines = 1,
                    label = { Text(text = "Area of Interest") })


                Button(
                    onClick = {

                        if (selectedImageUri == null) {

                            Toast.makeText(
                                context,
                                "Please upload a Profile Picture",
                                Toast.LENGTH_LONG
                            ).show()

                        } else if (TextUtils.isEmpty(name)) {

                            Toast.makeText(context, "Please enter Name", Toast.LENGTH_LONG)
                                .show()

                        } else if (TextUtils.isEmpty(number)) {

                            Toast.makeText(context, "Please enter Mobile Number", Toast.LENGTH_LONG)
                                .show()

                        } else if (number.length < 10) {

                            Toast.makeText(
                                context,
                                "Please enter valid Mobile Number",
                                Toast.LENGTH_LONG
                            )
                                .show()

                        } else if (TextUtils.isEmpty(email)) {

                            Toast.makeText(context, "Please enter Email", Toast.LENGTH_LONG)
                                .show()

                        } else if (TextUtils.isEmpty(areaOfInterest)) {

                            Toast.makeText(
                                context,
                                "Please enter Area of Interest",
                                Toast.LENGTH_LONG
                            )
                                .show()

                        } else {


                            val documentReference = firestore.collection("users").document(userId!!)

                            //Adding User Data to HashMap
                            val userHashMap: HashMap<String, Any> = HashMap()

                            userHashMap["fullName"] = name.trim()
                            userHashMap["mobileNumber"] = number.trim()
                            userHashMap["email"] = email.trim()
                            userHashMap["areaOfInterest"] = areaOfInterest.capitalizeWords().trim()
                            userHashMap["isProfileCompleted"] = true
                            userHashMap["isOnline"] = true
//                            userHashMap["profilePicture"] = selectedImageUri.toString()

                            //Adding User Image
                            storageReference =
                                storageReference.child(System.currentTimeMillis().toString())


                            storageReference.putFile(selectedImageUri!!)
                                .addOnSuccessListener { task ->

                                    storageReference.downloadUrl.addOnSuccessListener { uri ->

                                        // Update profilePicture in userHashMap with download URL
                                        userHashMap["profilePicture"] = uri.toString()


                                        //For Updating the User Data
//                                        documentReference.update(userHashMap as Map<String, Any>).addOnSuccessListener {
//                                            Toast.makeText(context, "User Profile Updated", Toast.LENGTH_SHORT).show()
//
//                                            // Update data and save to SharedPreferences
//                                            preferencesManager.saveProfileUpdated("profileCompleted", true)
//
//                                            context.startActivity(Intent(context, HomeActivity::class.java))
//
//
//                                        }


                                        documentReference.set(userHashMap).addOnSuccessListener {
                                            Toast.makeText(context, "User Profile Updated", Toast.LENGTH_SHORT).show()

                                            // Update data and save to SharedPreferences
                                            preferencesManager.saveProfileUpdated("profileCompleted", true)

                                            context.startActivity(Intent(context, HomeActivity::class.java))

                                            (context as Activity).finish()


                                        }.addOnFailureListener { e ->
                                            Toast.makeText(
                                                context,
                                                "Failed to update user profile: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        }

                                    }


                                }


                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Text(text = "Update", fontSize = 16.sp)

                }

            }
        }
    }

}


@Preview
@Composable
fun RegistrationPreview() {
    RegistrationUI()
}