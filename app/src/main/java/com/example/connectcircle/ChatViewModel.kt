package com.example.connectcircle

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.connectcircle.utils.Constants
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException

class ChatViewModel : ViewModel() {

    private val currentUserId = Firebase.auth.currentUser?.uid ?: ""

    private val _recipientId = MutableLiveData<String>()
    val recipientId: LiveData<String> = _recipientId

    private val _fullName = MutableLiveData<String>()
    val fullName: LiveData<String> = _fullName

    private val _senderName = MutableLiveData<String>()
    val senderName: LiveData<String> = _senderName

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

    fun setSenderName(senderName: String) {
        _senderName.value = senderName
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
    fun addMessage(context: Context, fcmToken: String, userId: String) {
        val message: String = _message.value ?: throw IllegalArgumentException("Message is empty")
        val recipientId =
            _recipientId.value ?: throw IllegalStateException("Recipient ID is not set")

        if (message.isNotEmpty()) {
            val chatId = getChatId(currentUserId, recipientId)


            val messageData = hashMapOf(
                Constants.MESSAGE to message,
                Constants.SENT_BY to currentUserId,
                Constants.SENT_TO to recipientId,
                Constants.SENT_ON to System.currentTimeMillis()
            )

            Firebase.firestore.collection("users")
                .document(currentUserId)
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .document() // Create a new message document
                .set(messageData)
                .addOnSuccessListener {
                    _message.value = ""

                    // Update the last message and timestamp in both user's chat subcollections
                    updateLastMessage(chatId, message, recipientId)

                    CoroutineScope(Dispatchers.IO).launch {
                        sendChatNotification(fcmToken, message, userId, context)
                    }

                }.addOnFailureListener { exception ->
                    // Handle the error if needed
                    Log.e("Chat", "Failed to send message: ", exception)
                }


            // Send the same message to the recipient's messages collection
            Firebase.firestore.collection("users")
                .document(recipientId)
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .document() // Create a new message document
                .set(messageData)
                .addOnFailureListener { exception ->
                    // Handle the error if needed
                    Log.e("Chat", "Failed to send message to recipient: ", exception)
                }

        }
    }

    private fun updateLastMessage(
        chatId: String,
        message: String,
        recipientId: String,
    ) {

        // Update last message for the current user
        Firebase.firestore.collection("users")
            .document(currentUserId)
            .collection("chats")
            .document(chatId)
            .set(
                hashMapOf(
                    "messageFrom" to _fullName.value,
                    "lastMessage" to message,
                    "lastMessageTimestamp" to System.currentTimeMillis(),
                    "recipientId" to recipientId
                )
            )

        // Update last message for the recipient user
        Firebase.firestore.collection("users")
            .document(recipientId)
            .collection("chats")
            .document(chatId)
            .set(
                hashMapOf(
                    "messageFrom" to _senderName.value,
                    "lastMessage" to message,
                    "lastMessageTimestamp" to System.currentTimeMillis(),
                    "recipientId" to recipientId
                )
            )

    }

    private fun sendChatNotification(
        fcmToken: String,
        message: String,
        userId: String,
        context: Context
    ) {

        if (userId.isEmpty()) {
            Log.w("ChatNotification", "Full Name is empty")
            return
        }

        // Construct the notification payload
        val payload = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", fcmToken)
                put("data", JSONObject().apply {
                    put("type", "chat_message")
                    put("senderName", userId)
                    put("message", message)
                })
                put("android", JSONObject().apply {
                    put("priority", "high")
                })
            })
        }

        // Send the notification
        val client = OkHttpClient()
        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            payload.toString()
        )
        val request = Request.Builder()
            .url("https://fcm.googleapis.com/v1/projects/connect-circle-23dca/messages:send")  // Update with your FCM URL
            .post(requestBody)
            .addHeader(
                "Authorization",
                "Bearer " + getServiceAccountAccessToken(context)
            )  // Replace with your server key
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("ChatNotification", "Failed to send notification: ", e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    Log.e("ChatNotification", "Error sending notification: ${response.message}")
                } else {
                    Log.d("ChatNotification", "Notification sent successfully.")
                }
            }
        })

    }

    @Throws(IOException::class)
    fun getServiceAccountAccessToken(context: Context): String {

        val serviceAccount = context.resources.openRawResource(R.raw.service_account_key)

        val credentials = GoogleCredentials.fromStream(serviceAccount)
            .createScoped("https://www.googleapis.com/auth/firebase.messaging")

        credentials.refresh()
        return credentials.accessToken.tokenValue

    }


    /**
     * Get the messages
     */
    private fun getMessages() {
        val recipientId = _recipientId.value ?: return
        val chatId = getChatId(currentUserId, recipientId)

        // Fetch messages from the current user's chats subcollection
        Firebase.firestore.collection("users")
            .document(currentUserId)
            .collection("chats")
            .document(chatId)
            .collection("messages")
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
