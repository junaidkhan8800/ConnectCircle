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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.twotone.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }


    val maxNumberCount = 10

    val context = LocalContext.current

    val preferencesManager = remember { PreferencesManager(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Register", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
            ) {

                item{

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

                                if (selectedImageUri != null){

                                    AsyncImage(
                                        modifier = Modifier.size(150.dp),
                                        model = selectedImageUri,
                                        contentDescription = "Profile Image",
                                        contentScale = ContentScale.Crop
                                    )

                                }else{

                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        modifier = Modifier.size(150.dp),
                                        contentDescription = "Profile Image",
                                    )

                                }

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



                        OutlinedTextField(value = mobileNumber,
                            onValueChange = {
                                if (it.length <= maxNumberCount) {
                                    mobileNumber = it
                                }
                            },
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

                        OutlinedTextField(value = password,
                            onValueChange = {
                                password = it
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            label = { Text(text = "Password") },
                            trailingIcon = {
                                val image = if (passwordVisible)
                                    Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff

                                val description = if (passwordVisible) "Hide Password" else "Show Password"

                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, description)
                                }
                            })

                        OutlinedTextField(value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            label = { Text(text = "Confirm Password") },
                            trailingIcon = {
                                val image = if (passwordVisible)
                                    Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff

                                val description = if (passwordVisible) "Hide Password" else "Show Password"

                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, description)
                                }
                            })


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Checkbox(checked = checked, onCheckedChange = { checked = it })
                            Text(
                                text = "Agree Terms and Conditions",
                                modifier = Modifier.padding(start = 4.dp),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

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

                                } else if (TextUtils.isEmpty(mobileNumber)) {

                                    Toast.makeText(
                                        context,
                                        "Please enter Mobile Number",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()

                                } else if (mobileNumber.length < 10) {

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

                                } else if (TextUtils.isEmpty(password)) {

                                    Toast.makeText(
                                        context,
                                        "Please enter Password",
                                        Toast.LENGTH_LONG
                                    ).show()

                                } else if (password.length < 6) {

                                    Toast.makeText(
                                        context,
                                        "Please enter 6 characters Password",
                                        Toast.LENGTH_LONG
                                    ).show()

                                } else if (TextUtils.isEmpty(confirmPassword)) {

                                    Toast.makeText(
                                        context,
                                        "Please enter Confirm Password",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()

                                } else if (password != confirmPassword) {

                                    Toast.makeText(
                                        context,
                                        "Passwords do not match",
                                        Toast.LENGTH_LONG
                                    ).show()

                                } else if (!checked) {

                                    Toast.makeText(
                                        context,
                                        "Please accept Terms and Conditions",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()

                                } else {

                                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {

                                        if (it.isSuccessful) {

                                            val documentReference =
                                                firestore.collection("users").document(it.result.user!!.uid)

                                            //Adding User Data to HashMap
                                            val userHashMap: HashMap<String, Any> = HashMap()

                                            userHashMap["fullName"] = name.trim()
                                            userHashMap["mobileNumber"] = mobileNumber.trim()
                                            userHashMap["email"] = email.trim()
                                            userHashMap["areaOfInterest"] =
                                                areaOfInterest.capitalizeWords().trim()
                                            userHashMap["password"] = password.trim()
                                            userHashMap["isOnline"] = true
                                            //userHashMap["profilePicture"] = selectedImageUri.toString()

                                            //Adding User Image
                                            storageReference =
                                                storageReference.child(System.currentTimeMillis().toString())


                                            storageReference.putFile(selectedImageUri!!)
                                                .addOnSuccessListener { task ->

                                                    storageReference.downloadUrl.addOnSuccessListener { uri ->

                                                        // Update profilePicture in userHashMap with download URL
                                                        userHashMap["profilePicture"] = uri.toString()


                                                        documentReference.set(userHashMap)
                                                            .addOnSuccessListener {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Registration Successful",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()

                                                                // Update data and save to SharedPreferences
                                                                preferencesManager.saveAreaOfInterest(
                                                                    "areaOfInterest",
                                                                    areaOfInterest.capitalizeWords().trim()
                                                                )

                                                                (context as Activity).finish()


                                                            }.addOnFailureListener { e ->
                                                                Toast.makeText(
                                                                    context,
                                                                    "Failed to register: ${e.message}",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                    }
                                                }

                                        }

                                    }.addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Failed to register: ${it.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {

                            Text(text = "Register", fontSize = 16.sp)

                        }

                    }
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