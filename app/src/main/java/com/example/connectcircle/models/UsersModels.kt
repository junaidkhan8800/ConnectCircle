package com.example.connectcircle.models

data class UsersModels(

    var id: String,

    var fullName: String = "",
    var mobileNumber: String = "",
    var email: String = "",
    var areaOfInterest: String = "",
    var profilePicture: String = "",

    var isOnline: Any?
)
