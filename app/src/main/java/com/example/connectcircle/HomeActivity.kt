package com.example.connectcircle

import android.os.Bundle
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.connectcircle.navigation.HomeScreen
import com.example.connectcircle.navigation.OnlineUsers
import com.example.connectcircle.navigation.ProfileScreen
import com.example.connectcircle.ui.theme.ConnectCircleTheme
import com.example.connectcircle.utils.Constants

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            ConnectCircleTheme {

                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Scaffold(
                        bottomBar = {
                            BottomNavigationBar(navController = navController)
                        },
                        content = { padding ->
                            NavHostContainer(navController = navController, padding = padding)
                        }
                    )

                }
            }
        }
    }
}

@Composable
fun NavHostContainer(
    navController: NavHostController,
    padding: PaddingValues
) {

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(paddingValues = padding),
        builder = {

            composable("home") {
                HomeScreen()
            }
            composable("online") {
                OnlineUsers()
            }
            composable("profile") {
                ProfileScreen()
            }
        }
    )

}

@Composable
fun BottomNavigationBar(navController : NavHostController) {

   BottomAppBar {
       val navBackStackEntry by navController.currentBackStackEntryAsState()
       val currentRoute = navBackStackEntry?.destination?.route

       Constants.BottomNavItems.forEach { navItem ->

           NavigationBarItem(selected = currentRoute == navItem.route,
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