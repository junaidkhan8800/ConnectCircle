package com.example.connectcircle

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object PresenceManager {

    @SuppressLint("StaticFieldLeak")
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun updatePresence(userId: String, isOnline: Boolean) {
        val userRef = firestore.collection("users").document(userId)
        val updates = hashMapOf<String, Any>(
            "isOnline" to isOnline,

            )
        userRef.update(updates)
            .addOnSuccessListener {
                // Update successful
                Log.d("updatePresence", "Presence updated")
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.d("updatePresence", "Failed to update presence: ${e.message}")
            }
    }
}
