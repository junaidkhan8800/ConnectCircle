package com.example.connectcircle.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import com.example.connectcircle.models.BottomNavItem
import com.permissionx.guolindev.PermissionX

class Constants {

    companion object {

        const val appId : Long = 150939430
        const val appSign = "7baacd535f0cb1ff1e014916eda9107a2e0ae056507243a685081a6cf12c4c45"

        const val TAG = "connect-circle"

        const val MESSAGES = "messages"
        const val MESSAGE = "message"
        const val SENT_BY = "sent_by"
        const val SENT_ON = "sent_on"
        const val SENT_TO = "sent_to"
        const val CHATS = "chats"
        const val RECIPIENT_ID = "recipient_id"
        const val IS_CURRENT_USER = "is_current_user"

        //Extension Function
        fun String.capitalizeWords(delimiter: String = " ") =
            split(delimiter).joinToString(delimiter) { word ->

                val smallCaseWord = word.lowercase()
                smallCaseWord.replaceFirstChar(Char::titlecaseChar)

            }


        val BottomNavItems = listOf(

            BottomNavItem(label = "Home", icon = Icons.Filled.Home, route = "home"),
            BottomNavItem(label = "Online Users", icon = Icons.Filled.Groups, route = "online"),
            BottomNavItem(label = "Profile", icon = Icons.Filled.Person, route = "profile")

        )




    }

}
