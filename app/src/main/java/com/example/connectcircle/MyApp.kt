package com.example.connectcircle

import android.app.Application
import com.example.connectcircle.utils.Constants
import com.google.firebase.FirebaseApp

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

    }


//    fun startVideoCall(targetUserId : String){
//
//        val targetUserName = targetUserId
//
//        videoCallButton.setIsVideoCall(true)
//        videoCallButton.resourceId = "zego_uikit_call"
//        videoCallButton.setInvitees(listOf(ZegoUIKitUser(targetUserId,targetUserName)))
//
//
//    }


//    fun startVoiceCall(targetUserId : String){
//
//        val targetUserName = targetUserId
//
//        voiceCallButton.setIsVideoCall(false)
//        voiceCallButton.resourceId = "zego_uikit_call"
//        voiceCallButton.setInvitees(listOf(ZegoUIKitUser(targetUserId,targetUserName)))
//
//
//    }

}