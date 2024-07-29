package com.example.connectcircle.models

import com.google.firebase.firestore.PropertyName

data class UsersModels(

    @get:PropertyName("id") var id: String,
    @get:PropertyName("fullName") var fullName: String = "",
    @get:PropertyName("mobileNumber") var mobileNumber: String = "",
    @get:PropertyName("email") var email: String = "",
    @get:PropertyName("areaOfInterest") var areaOfInterest: String = "",
    @get:PropertyName("profilePicture") var profilePicture: String = "",
    @get:PropertyName("isOnline") var isOnline: Any?
){

    // Empty constructor required for Firestore deserialization
    constructor() : this("", "", "", "", "", "", true)
}
