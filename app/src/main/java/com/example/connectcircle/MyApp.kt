package com.example.connectcircle

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MyApp : Application() {

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)

        Log.e("ApplicationClass", "onCreate")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Video Call Notifications"
            val descriptionText = "Notifications for incoming video calls"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("YOUR_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }



    }

    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {
            if (++activityReferences == 1 && !isActivityChangingConfigurations) {
                // App enters foreground
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                Log.e("ApplicationClass", "App in foreground")
                if (!userId.isNullOrEmpty()) {
                    makeUserOnline(userId)
                }
            }
        }

        override fun onActivityResumed(activity: Activity) {}

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {
            isActivityChangingConfigurations = activity.isChangingConfigurations
            if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                // App enters background
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                Log.e("ApplicationClass", "App in background")
                if (!userId.isNullOrEmpty()) {
                    makeUserOffline(userId)
                }
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}
    }

    private fun makeUserOnline(userId: String?) {
        // Create a partial UsersModels object with only the online status
        val userStatusUpdate = mapOf("isOnline" to true)

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId!!)

        // Firestore: Update the existing document without overwriting other fields
        userRef.set(userStatusUpdate, SetOptions.merge())
            .addOnSuccessListener { Log.e("ApplicationClass", "User is online") }
            .addOnFailureListener { e -> Log.e("ApplicationClass", "Error updating user status", e) }

        // Firebase Realtime Database status
        FirebaseDatabase.getInstance().getReference("status/$userId").setValue("online")

        // Set up the disconnect hook to mark offline when connection is lost
        FirebaseDatabase.getInstance().getReference("/status/$userId")
            .onDisconnect()
            .setValue("offline")
    }

    fun makeUserOffline(userId: String?) {
        // Create a partial UsersModels object with only the offline status
        val userStatusUpdate = mapOf("isOnline" to false)

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId!!)

        // Firestore: Update the existing document without overwriting other fields
        userRef.set(userStatusUpdate, SetOptions.merge())
            .addOnSuccessListener { Log.e("ApplicationClass", "User is offline") }
            .addOnFailureListener { e -> Log.e("ApplicationClass", "Error updating user status", e) }

        // Firebase Realtime Database status
        FirebaseDatabase.getInstance().getReference("status/$userId").setValue("offline")
    }
}
