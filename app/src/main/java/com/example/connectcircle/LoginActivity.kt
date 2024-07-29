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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
        val isProfileCompleted = preferencesManager.getProfileUpdated("profileCompleted", true)


        mAuth.currentUser?.uid?.let { PresenceManager.updatePresence(it, true) }

        if (mAuth.currentUser != null && isProfileCompleted){

            startActivity(Intent(this, HomeActivity::class.java))
        }else if (mAuth.currentUser != null && !isProfileCompleted){

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
                    Toast.makeText(this@LoginActivity, "Failed to fetch profile completion status: ${e.message}", Toast.LENGTH_SHORT).show()
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

    var mobileNumber by remember { mutableStateOf("") }

    var otp by remember { mutableStateOf("") }

    var otpVisibility by remember { mutableStateOf(false) }

    var sendOtpVisibility by remember { mutableStateOf(true) }

    val maxNumberCount = 10

    var verificationID by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    // on below line creating variable
    // for firebase auth and callback
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
//    mAuth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks


    // on below line creating callback
    callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            // on below line updating message
            // and displaying toast message
            message = "Verification successful"
            Toast.makeText(context, "Verification Successful", Toast.LENGTH_LONG).show()
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            // on below line displaying error as toast message.
            message = "Fail to verify user : \n" + p0.message
            Toast.makeText(context, "Verification Failed", Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(verificationId: String, p1: PhoneAuthProvider.ForceResendingToken) {
            // this method is called when code is send
            super.onCodeSent(verificationId, p1)
            verificationID = verificationId

            otpVisibility = true
            sendOtpVisibility = false
        }
    }

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
                    text = "Enter your Mobile Number. We will send you a One Time Password (OTP) on your provided mobile number.",
                    modifier = Modifier.padding(16.dp), fontSize = 16.sp,
                )

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
                    label = { Text(text = "Enter Mobile Number") })

                AnimatedVisibility(visible = otpVisibility) {

                    OutlinedTextField(value = otp, onValueChange = { otp = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        maxLines = 1,
                        label = { Text(text = "Enter OTP") })

                }

                Row(modifier = Modifier.fillMaxWidth()) {

                    AnimatedVisibility(
                        visible = sendOtpVisibility, modifier = Modifier
                            .weight(0.5F)
                            .padding(16.dp)
                    ) {

                        Button(onClick = {


                            if (TextUtils.isEmpty(mobileNumber)) {
                                Toast.makeText(context, "Please enter Mobile Number", Toast.LENGTH_LONG)
                                    .show()
                            } else if (mobileNumber.length < 10){
                                Toast.makeText(context, "Please enter valid Mobile Number", Toast.LENGTH_LONG)
                                    .show()
                            }
                            else {

                                val number = "+91${mobileNumber}"
                                // on below line calling method to generate verification code.
                                sendVerificationCode(number, mAuth, context as Activity, callbacks)
                            }

                        }) {

                            Text(text = "Send OTP", fontSize = 16.sp)

                        }

                    }

                    AnimatedVisibility(
                        visible = otpVisibility, modifier = Modifier
                            .weight(0.5F)
                            .padding(16.dp)
                    ) {

                        Button(
                            onClick = {

                                // on below line we are validating
                                // user input parameters.
                                if (TextUtils.isEmpty(otp)) {
                                    // displaying toast message on below line.
                                    Toast.makeText(context, "Please enter OTP", Toast.LENGTH_LONG)
                                        .show()
                                } else {
                                    // on below line generating phone credentials.
                                    val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                                        verificationID, otp
                                    )
                                    // on below line signing within credentials.
                                    signInWithPhoneAuthCredential(
                                        mobileNumber,
                                        credential,
                                        mAuth,
                                        context as Activity,
                                        context,
                                        message
                                    )
                                }

                            },
                        ) {

                            Text(text = "Verify OTP", fontSize = 16.sp)

                        }
                    }
                }
            }
        }
    }
}

// on below line creating method to
// sign in with phone credentials.
private fun signInWithPhoneAuthCredential(
    mobileNumber: String,
    credential: PhoneAuthCredential,
    auth: FirebaseAuth,
    activity: Activity,
    context: Context,
    message: String
) {
    // on below line signing with credentials.
    auth.signInWithCredential(credential)
        .addOnCompleteListener(activity) { task ->
            // displaying toast message when
            // verification is successful
            if (task.isSuccessful) {
//                message = "Verification successful"

                val preferencesManager = PreferencesManager(context)
                preferencesManager.saveMobileNumber("mobileNumber", mobileNumber)

                Toast.makeText(context, "Verification Successful", Toast.LENGTH_LONG).show()

                val isProfileCompleted = preferencesManager.getProfileUpdated("profileCompleted", true)

                if (!isProfileCompleted){

                    context.startActivity(
                        Intent(
                            context,
                            RegistrationActivity::class.java
                        )
                    )
                }else{
                    context.startActivity(
                        Intent(
                            context,
                            HomeActivity::class.java
                        )
                    )
                }



                activity.finish()

            } else {
                // Sign in failed, display a message
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    // The verification code
                    // entered was invalid
                    Toast.makeText(
                        context,
                        "Verification failed" + (task.exception as FirebaseAuthInvalidCredentialsException).message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
}

// below method is use to send
// verification code to user phone number.
private fun sendVerificationCode(
    number: String,
    auth: FirebaseAuth,
    activity: Activity,
    callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
) {
    // on below line generating options for verification code
    val options = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber(number) // Phone number to verify
        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
        .setActivity(activity) // Activity (for callback binding)
        .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
        .build()
    PhoneAuthProvider.verifyPhoneNumber(options)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginActivity() {
    val context = LocalContext.current
    LoginActivityUI(context)
}