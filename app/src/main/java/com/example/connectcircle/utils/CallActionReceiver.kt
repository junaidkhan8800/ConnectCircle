package com.example.connectcircle.utils

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val callId = intent.getStringExtra("callId")
        val action = intent.action

        if (action == "ACCEPT_CALL") {
            val options = JitsiMeetConferenceOptions.Builder()
                .setRoom(callId)
                .setFeatureFlag("invite.enabled", false)
                .setFeatureFlag("lobby-mode.enabled", false)
                .setFeatureFlag("prejoinpage.enabled", false)
//                                .setFeatureFlag("tile-view.enabled",true)
                .setFeatureFlag("welcomepage.enabled", false)
                .build()
            JitsiMeetActivity.launch(context, options)
        } else if (action == "REJECT_CALL") {
            // Handle call rejection, e.g., send a rejection notification to the caller.

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1001)  // Use the same notification ID you used to create the notification

        }
    }
}
