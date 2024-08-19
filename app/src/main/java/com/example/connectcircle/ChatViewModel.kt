package com.example.connectcircle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.connectcircle.utils.Constants
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatViewModel : ViewModel() {

    private val currentUserId = Firebase.auth.currentUser?.uid ?: ""

    private val _recipientId = MutableLiveData<String>()
    val recipientId: LiveData<String> = _recipientId

    private val _fullName = MutableLiveData<String>()
    val fullName: LiveData<String> = _fullName

    private val _profilePicture = MutableLiveData<String>()
    val profilePicture: LiveData<String> = _profilePicture

    private val _message = MutableLiveData("")
    val message: LiveData<String> = _message

    private val _messages = MutableLiveData<MutableList<Map<String, Any>>>(mutableListOf())
    val messages: LiveData<MutableList<Map<String, Any>>> = _messages

    fun setRecipientId(recipientId: String) {
        _recipientId.value = recipientId
        getMessages()
    }

    fun setFullName(fullName: String) {
        _fullName.value = fullName
    }

    fun setProfilePicture(profilePicture: String) {
        _profilePicture.value = profilePicture
    }

    /**
     * Update the message value as the user types
     */
    fun updateMessage(message: String) {
        _message.value = message
    }

    /**
     * Send a message
     */
    fun addMessage() {
        val message: String = _message.value ?: throw IllegalArgumentException("Message is empty")
        val recipientId =
            _recipientId.value ?: throw IllegalStateException("Recipient ID is not set")

        if (message.isNotEmpty()) {
            val chatId = getChatId(currentUserId, recipientId)
            Firebase.firestore.collection(Constants.CHATS).document(chatId)
                .collection(Constants.MESSAGES).document().set(
                    hashMapOf(
                        Constants.MESSAGE to message,
                        Constants.SENT_BY to currentUserId,
                        Constants.SENT_TO to recipientId,
                        Constants.SENT_ON to System.currentTimeMillis()
                    )
                ).addOnSuccessListener {
                    _message.value = ""
                }
        }
    }

    /**
     * Get the messages
     */
    private fun getMessages() {
        val recipientId = _recipientId.value ?: return
        val chatId = getChatId(currentUserId, recipientId)

        Firebase.firestore.collection(Constants.CHATS).document(chatId)
            .collection(Constants.MESSAGES)
            .orderBy(Constants.SENT_ON)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                val list = mutableListOf<Map<String, Any>>()

                if (value != null) {
                    for (doc in value) {
                        val data = doc.data
                        data[Constants.IS_CURRENT_USER] =
                            currentUserId == data[Constants.SENT_BY].toString()
                        list.add(data)
                    }
                }

                updateMessages(list)
            }
    }

    /**
     * Update the list after getting the details from Firestore
     */
    private fun updateMessages(list: MutableList<Map<String, Any>>) {
        _messages.value = list.asReversed()
    }

    /**
     * Generate a unique chat ID based on the user IDs
     */
    private fun getChatId(user1: String, user2: String): String {
        return if (user1.isNotEmpty() && user2.isNotEmpty()) {
            if (user1 < user2) {
                "${user1}_$user2"
            } else {
                "${user2}_$user1"
            }
        } else {
            throw IllegalArgumentException("User IDs cannot be empty")
        }
    }

}
