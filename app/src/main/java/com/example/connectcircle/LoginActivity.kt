package com.example.connectcircle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.connectcircle.ui.theme.ConnectCircleTheme
import com.example.connectcircle.utils.PreferencesManager
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class LoginActivity : ComponentActivity() {

    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        val preferencesManager = PreferencesManager(this)

        // Update data and save to SharedPreferences
        val isProfileCompleted = preferencesManager.getProfileUpdated("profileCompleted", false)


        mAuth.currentUser?.uid?.let { PresenceManager.updatePresence(it, true) }

        if (mAuth.currentUser != null && isProfileCompleted) {

            startActivity(Intent(this, HomeActivity::class.java))
        } else if (mAuth.currentUser != null && !isProfileCompleted) {

            startActivity(Intent(this, RegistrationActivity::class.java))

        }

        enableEdgeToEdge()
        setContent {
            ConnectCircleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginActivityUI(this)
                }
            }
        }
    }


    private fun checkProfileCompletion(uid: String): Boolean {
        var isProfileCompleted = false
        // Using withContext to switch to IO dispatcher for Firestore operation
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                isProfileCompleted = document.getBoolean("isProfileCompleted") ?: false
            }
            .addOnFailureListener { e ->
                // Handle errors while fetching profile completion status
                Toast.makeText(
                    this@LoginActivity,
                    "Failed to fetch profile completion status: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        return isProfileCompleted
    }

    // Function to handle profile completion status
    private fun handleProfileCompletion(isProfileCompleted: Boolean) {
        if (isProfileCompleted) {
            // Profile is completed, navigate to HomeActivity
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            // Profile is not completed, navigate to RegistrationActivity
            startActivity(Intent(this, RegistrationActivity::class.java))
            finish()
        }
    }

}

@Composable
fun LoginActivityUI(context: Context) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.primary),
    ) {

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.4f),
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
                .weight(0.6f),
            shape = RoundedCornerShape(topStart = 50.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                Text(
                    text = "Login", fontSize = 32.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp, start = 16.dp)
                )

                Text(
                    text = "Enter your Email and Password to proceed with login.",
                    modifier = Modifier.padding(16.dp), fontSize = 16.sp,
                )

                OutlinedTextField(value = email,
                    onValueChange = { email = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true,
                    label = { Text(text = "Email") })



                OutlinedTextField(value = password, onValueChange = { password = it },
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

                Text(
                    text = "Forgot Password?",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),onClick = {

                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(context, "Please enter Email", Toast.LENGTH_LONG).show()
                    } else if (TextUtils.isEmpty(password)) {
                        Toast.makeText(context, "Please enter Password", Toast.LENGTH_LONG).show()
                    } else {

                    }

                }) {
                    Text(text = "Login", fontSize = 16.sp)
                }

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {

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
                            }, fontSize = 16.sp,
                    )

                }

            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginActivity() {
    val context = LocalContext.current
    LoginActivityUI(context)
}