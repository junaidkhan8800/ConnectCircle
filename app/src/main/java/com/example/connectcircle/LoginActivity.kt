package com.example.connectcircle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.connectcircle.ui.theme.ConnectCircleTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import android.Manifest


class LoginActivity : ComponentActivity() {

    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        if (mAuth.currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        enableEdgeToEdge()
        setContent {
            ConnectCircleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {


                    // Check and request notification permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestNotificationPermission()
                        }
                    }


                    LoginActivityUI(this)
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }
    }
}

@Composable
fun LoginActivityUI(context: Context) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }

    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    Scaffold(Modifier.fillMaxSize()) { it ->

        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .background(color = MaterialTheme.colorScheme.primary),
            ) {

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.3f),
                    color = MaterialTheme.colorScheme.primary
                ) {

                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "null",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.7f),
                    shape = RoundedCornerShape(topStart = 50.dp)
                ) {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())) {

                        Text(
                            text = "Login", fontSize = 32.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 24.dp, start = 16.dp)
                        )

                        Text(
                            text = "Enter your Email and Password to proceed with login.",
                            modifier = Modifier.padding(16.dp), fontSize = 16.sp,
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            singleLine = true,
                            label = { Text(text = "Email") }
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
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

                                val description =
                                    if (passwordVisible) "Hide Password" else "Show Password"

                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, description)
                                }
                            })

                        Text(
                            text = "Forgot Password?",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp), onClick = {

                                if (TextUtils.isEmpty(email)) {
                                    Toast.makeText(context, "Please enter Email", Toast.LENGTH_LONG)
                                        .show()
                                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    Toast.makeText(
                                        context,
                                        "Please enter Email in correct format",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else if (TextUtils.isEmpty(password)) {
                                    Toast.makeText(context, "Please enter Password", Toast.LENGTH_LONG)
                                        .show()
                                } else {

                                    isLoading = true

                                    mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener {

                                            if (it.isSuccessful) {
                                                Toast.makeText(
                                                    context,
                                                    "Login successful",
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                isLoading = false

                                                updateDeviceTokenOnLogin()

                                                context.startActivity(
                                                    Intent(
                                                        context,
                                                        HomeActivity::class.java
                                                    )
                                                )
                                                (context as Activity).finish()
                                            } else {

                                                isLoading = false

                                                Toast.makeText(
                                                    context,
                                                    "Login failed. ${it.exception?.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                }
                            }) {
                            Text(text = "Login", fontSize = 16.sp)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {

                            Text(
                                text = "Don't have an account?",
                                fontSize = 16.sp
                            )

                            Text(
                                text = "Register",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .clickable {
                                        context.startActivity(
                                            Intent(
                                                context,
                                                RegistrationActivity::class.java
                                            )
                                        )
                                    },
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }

            // Loading Dialog
            if (isLoading) {
                Dialog(onDismissRequest = {}) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.White, shape = RoundedCornerShape(10.dp))
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

fun updateDeviceTokenOnLogin() {
    // Get the current user ID
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    if (userId != null) {
        // Retrieve the current FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Update Firestore with the new token
            saveTokenToFirestore(userId, token)
        }
    } else {
        Log.w("FCM", "User not logged in")
    }
}

fun saveTokenToFirestore(userId: String, token: String) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(userId)

    val userFcmToken = mapOf("fcmToken" to token)

    userRef.set(userFcmToken, SetOptions.merge())
        .addOnSuccessListener { Log.e("FCMTOKEN", "Success") }
        .addOnFailureListener { e -> Log.e("FCMTOKEN", "Failure", e) }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginActivity() {
    val context = LocalContext.current
    LoginActivityUI(context)
}
