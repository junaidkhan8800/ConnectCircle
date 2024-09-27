package com.yusuf.connectcircle

import android.os.Bundle
import android.util.Log
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
import com.yusuf.connectcircle.models.UsersModels
import com.yusuf.connectcircle.navigation.HomeScreen
import com.yusuf.connectcircle.navigation.PastChatsScreen
import com.yusuf.connectcircle.navigation.ProfileScreen
import com.yusuf.connectcircle.ui.theme.ConnectCircleTheme
import com.yusuf.connectcircle.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

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

                        userDocumentId = mAuth.currentUser?.uid!!

//                        getOnlineUsers(user.areaOfInterest)

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

//    private fun getOnlineUsers(areaOfInterest: String) {
//
//        mFirestore.collection("users")
//            .get()
//            .addOnSuccessListener { documents ->
//                CoroutineScope(Dispatchers.Default).launch {
//                    try {
//                        if (documents != null) {
//                            usersList.clear()
//
//                            // In-memory filtering
//                            val filteredDocuments = documents.filter { document ->
//                                val areaOfInterest1 = document.getString("areaOfInterest") ?: ""
//                                areaOfInterest1.contains(areaOfInterest.capitalizeWords().trim(), ignoreCase = true)
//                            }
//
//                            withContext(Dispatchers.Main) {
//                                for (document in filteredDocuments) {
//                                    Log.d("TAG", "${document.id} => ${document.data}")
//
//                                    if (document.id != mAuth.currentUser?.uid && document.get("isOnline") == true){
//
//                                        usersList.add(
//                                            UsersModels(
//                                                document.id,
//                                                document.get("fullName").toString(),
//                                                document.get("mobileNumber").toString(),
//                                                document.get("email").toString(),
//                                                document.get("areaOfInterest").toString(),
//                                                document.get("profilePicture").toString(),
//                                                document.get("isOnline"),
//                                                document.get("fcmToken").toString()
//                                            )
//                                        )
//
//                                    }
//                                }
//
//                            }
//                        } else {
//                            withContext(Dispatchers.Main){
//                                Toast.makeText(this@HomeActivity, "No such Users", Toast.LENGTH_LONG).show()
//                            }
//
//                        }
//                    } catch (ex: Exception) {
//                        ex.message?.let { Log.e("TAG", it) }
//                    }
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("TAG", "Error writing document", e)
//            }
//
//    }
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
                HomeScreen(userDocumentId,userData)
            }
            composable("online") {
                PastChatsScreen(userDocumentId, userData)
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