package com.example.connectcircle.models

import com.google.firebase.firestore.PropertyName

data class UsersModels(

    @get:PropertyName("id") var id: String = "",
    @get:PropertyName("fullName") var fullName: String = "",
    @get:PropertyName("mobileNumber") var mobileNumber: String = "",
    @get:PropertyName("email") var email: String = "",
    @get:PropertyName("areaOfInterest") var areaOfInterest: String = "",
    @get:PropertyName("profilePicture") var profilePicture: String = "",
    @get:PropertyName("isOnline") var isOnline: Any? = null,
    @get:PropertyName("fcmToken") var fcmToken: String = "",
    @get:PropertyName("messageFrom") var messageFrom: String = "",
    @get:PropertyName("lastMessage") var lastMessage: String = "",  // New field to store the last message
    @get:PropertyName("lastMessageTimestamp") var lastMessageTimestamp: Long = 0L,  // New field to store the timestamp of the last message
    @get:PropertyName("recipientId") var recipientId: String = ""  // New field to store the timestamp of the last message
) {
    // Empty constructor required for Firestore deserialization
    constructor() : this("", "", "", "", "", "", null, "","", "", 0L,"")
}
