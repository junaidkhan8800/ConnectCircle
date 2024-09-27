package com.yusuf.connectcircle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yusuf.connectcircle.models.UsersModels
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

        val userDocumentId = mAuth.currentUser?.uid ?: ""

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
            .addOnSuccessListener { chatDocuments ->
                val chatList = mutableListOf<UsersModels>()

                if (chatDocuments.isEmpty) {
                    _pastChats.value = chatList  // No chats available
                    return@addOnSuccessListener
                }

                for (chatDocument in chatDocuments) {
                    val chatData = chatDocument.toObject(UsersModels::class.java)

                    // Fetch the profile picture for the recipient of each chat
                    firestore.collection("users")
                        .document(chatData.recipientId)
                        .get()
                        .addOnSuccessListener { userDocument ->
                            if (userDocument != null && userDocument.exists()) {
                                // Get profile picture
                                val profilePicture = userDocument.getString("profilePicture") ?: ""
                                chatData.profilePicture = profilePicture  // Set profile picture in the chat model
                            }

                            // Add chat data to the list after fetching profile picture
                            chatList.add(chatData)

                            // Update LiveData once all chat data is processed
                            if (chatList.size == chatDocuments.size()) {
                                _pastChats.value = chatList
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("PastChatsViewModel", "Failed to get user data: ", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Handle error, possibly show an error message in the UI
                Log.e("PastChatsViewModel", "Failed to get chats: ", exception)
                _pastChats.value = emptyList()  // Set to empty list on failure
            }
    }
}
