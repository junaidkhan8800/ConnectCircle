package com.example.connectcircle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.connectcircle.ui.theme.ConnectCircleTheme
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConnectCircleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    ForgotPasswordScreenUI(this)
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordScreenUI(context: Context) {
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
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())) {

                        Text(
                            text = "Reset Password", fontSize = 32.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 24.dp, start = 16.dp)
                        )

                        Text(
                            text = "Enter your Email to get a password reset link.",
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
                                }
                                else {

                                    isLoading = true

                                    mAuth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener { task ->

                                            if (task.isSuccessful) {

                                                isLoading = false

                                                Toast.makeText(
                                                    context,
                                                    "Password reset email sent.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                (context as Activity).finish() // Close the activity after email is sent
                                            } else {

                                                isLoading = false

                                                Toast.makeText(
                                                    context,
                                                    "Error: ${task.exception?.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }

                                }
                            }) {
                            Text(text = "Reset Password", fontSize = 16.sp)
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

@Preview
@Composable
private fun ForgotPassPrev() {
    ForgotPasswordScreenUI(context = LocalContext.current)
}