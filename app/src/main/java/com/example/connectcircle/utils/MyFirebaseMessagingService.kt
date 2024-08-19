package com.example.connectcircle.utils

import android.Manifest
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.connectcircle.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            val type = remoteMessage.data["type"]
            val callerId = remoteMessage.data["callerId"]
            val callId = remoteMessage.data["callId"]

            val fullName = remoteMessage.data["senderName"]
            val message = remoteMessage.data["message"]

            if (type == "video_call") {
                showIncomingCallNotification(callerId, callId)
            }else if (type == "chat_message"){
                showChatMessageNotification(fullName, message)
            }
        }
    }

    private fun showChatMessageNotification(fullName: String?, message: String?) {

        val notification = NotificationCompat.Builder(this, "YOUR_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(fullName)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(1001, notification)

    }

    private fun showIncomingCallNotification(callerId: String?, callId: String?) {
        val acceptIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = "ACCEPT_CALL"
            putExtra("callId", callId)
        }
        val acceptPendingIntent = PendingIntent.getBroadcast(
            this, 0, acceptIntent, FLAG_IMMUTABLE
        )

        val rejectIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = "REJECT_CALL"
            putExtra("callId", callId)
        }
        val rejectPendingIntent = PendingIntent.getBroadcast(
            this, 1, rejectIntent, FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "YOUR_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Incoming Video Call")
            .setContentText("Caller is trying to reach you")
            .addAction(R.drawable.call_answer, "Accept", acceptPendingIntent)
            .addAction(R.drawable.call_end, "Reject", rejectPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(1001, notification)
    }
}
