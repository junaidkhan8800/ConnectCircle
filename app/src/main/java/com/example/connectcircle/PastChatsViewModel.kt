package com.example.connectcircle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.connectcircle.models.UsersModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PastChatsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    // LiveData to hold past chats list
    private val _pastChats = MutableLiveData<List<UsersModels>>()
    val pastChats: LiveData<List<UsersModels>> get() = _pastChats

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // Function to fetch past chats for the current user
    fun fetchPastChats() {

        val userDocumentId = mAuth.currentUser?.uid!!

        if (userDocumentId.isEmpty()) {
            Log.e("PastChatsViewModel", "User document ID is empty.")
            _pastChats.value = emptyList()  // Set to empty if ID is invalid
            return
        }

        firestore.collection("users")
            .document(userDocumentId)
            .collection("chats")
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)  // Sort by last message time
            .get()
            .addOnSuccessListener { documents ->
                val chatList = mutableListOf<UsersModels>()
                for (document in documents) {
                    val chatData = document.toObject(UsersModels::class.java)
                    chatList.add(chatData)
                }
                _pastChats.value = chatList  // Update LiveData with chat list
            }
            .addOnFailureListener { exception ->
                // Handle error, possibly show an error message in the UI
                Log.e("PastChatsViewModel", "Failed to get messages: ", exception)
                _pastChats.value = emptyList()  // Set to empty list on failure
            }
    }
}
