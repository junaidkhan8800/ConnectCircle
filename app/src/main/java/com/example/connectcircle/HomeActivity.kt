package com.example.connectcircle

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.connectcircle.models.UsersModels
import com.example.connectcircle.navigation.HomeScreen
import com.example.connectcircle.navigation.OnlineUsers
import com.example.connectcircle.navigation.ProfileScreen
import com.example.connectcircle.ui.theme.ConnectCircleTheme
import com.example.connectcircle.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig

class HomeActivity : ComponentActivity() {

    private val mFirestore = FirebaseFirestore.getInstance()
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var userData = UsersModels()
    private val usersList = mutableStateListOf<UsersModels>()
    var userDocumentId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            ConnectCircleTheme {

                val navController = rememberNavController()

                LaunchedEffect(key1 = true) {

                    mFirestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

                    getUserData()

                }


                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Scaffold(
                        bottomBar = {
                            BottomNavigationBar(navController = navController)
                        },
                        content = { padding ->
                            NavHostContainer(navController = navController,
                                padding = padding,
                                userData,
                                usersList,
                                userDocumentId)
                        }
                    )

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ZegoUIKitPrebuiltCallService.unInit()
    }

    private fun getUserData() {

        mFirestore
            .collection("users")
            .document(mAuth.currentUser?.uid!!)
            .get()
            .addOnSuccessListener { document ->

                if (document != null && document.exists()) {
                    val user = document.toObject(UsersModels::class.java)
                    if (user != null) {


                        userData.profilePicture = user.profilePicture
                        userData.fullName = user.fullName
                        userData.mobileNumber = user.mobileNumber
                        userData.email = user.email
                        userData.areaOfInterest = user.areaOfInterest

                        userDocumentId = document.id

                        val application : Application = application
                        val appId : Long = 150939430
                        val appSign = "7baacd535f0cb1ff1e014916eda9107a2e0ae056507243a685081a6cf12c4c45"
                        val userName : String = user.fullName
                        val userId : String = document.id
                        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
                        ZegoUIKitPrebuiltCallService.init(application,appId,appSign,userId,userName,callInvitationConfig)

                        getOnlineUsers(user.areaOfInterest)

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

    private fun getOnlineUsers(areaOfInterest: String) {

        mFirestore
            .collection("users")
            .whereEqualTo("areaOfInterest", areaOfInterest)
            .get()
            .addOnSuccessListener { documents ->
                try {
                    if (documents != null) {
                        for (document in documents) {
                            Log.d("TAG", "${document.id} => ${document.data}")

                            usersList.clear()

                            if (document.get("isOnline") == true){

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


                        }


//                    Toast.makeText(context, "DocumentSnapshot read successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "No such Users", Toast.LENGTH_LONG)
                            .show()
                    }
                } catch (ex: Exception) {
                    ex.message?.let { Log.e("TAG", it) }
                }
            }.addOnFailureListener { e ->
                Log.e("TAG", "Error writing document", e)
            }


    }
}

@Composable
fun NavHostContainer(
    navController: NavHostController,
    padding: PaddingValues,
    userData: UsersModels,
    userList: SnapshotStateList<UsersModels>,
    userDocumentId: String
) {

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(paddingValues = padding),
        builder = {

            composable("home") {
                HomeScreen(userDocumentId)
            }
            composable("online") {
                OnlineUsers(userList,userDocumentId)
            }
            composable("profile") {
                ProfileScreen(userData)
            }
        }
    )

}

@Composable
fun BottomNavigationBar(navController: NavHostController) {

    BottomAppBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Constants.BottomNavItems.forEach { navItem ->

            NavigationBarItem(
                selected = currentRoute == navItem.route,
                onClick = {
                    navController.navigate(navItem.route)
                },
                icon = {
                    Icon(imageVector = navItem.icon, contentDescription = navItem.label)
                },
                label = {
                    Text(text = navItem.label)
                },
                alwaysShowLabel = true
            )

        }

    }

}