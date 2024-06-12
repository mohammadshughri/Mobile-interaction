package de.luh.hci.mi.experiencesampling.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.luh.hci.mi.experiencesampling.R

// This component receives alarms and triggers a notification for each alarm.
// The alarm is registered in ExperienceSampling::scheduleNextNotification.
// https://developer.android.com/develop/ui/views/notifications
class NotificationTrigger : BroadcastReceiver() {

    // Theis broadcast receiver is registered to receive alarms.
    override fun onReceive(context: Context, intent: Intent) {
        showNotification(context)
    }

    // Notifications are posted on notification channels, so create one if needed.
    private fun createNotificationChannel(context: Context) {
        val channelId = context.getString(R.string.channel_id)
        val notificationManager = NotificationManagerCompat.from(context)
        var channel = notificationManager.getNotificationChannel(channelId)
        log("channel: $channel")
        if (channel == null) {
            val name = context.getString(R.string.channel_name)
            val description = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 200, 500, 400, 500, 400, 500, 200, 500)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Shows a notification. Clicking on the notification starts the sampling activity.
    private fun showNotification(context: Context) {
        createNotificationChannel(context)

        val intent = Intent(context, SamplingActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val channelId = context.getString(R.string.channel_id)

        // Define an array of string resource IDs that represent the questions
        val questionIds = arrayOf(
            R.string.effectiveness_question,
            R.string.clearness_question,
            R.string.exciting_question,
            R.string.predictable_question,
            R.string.additional_question,
            R.string.additional_question2
        )

        val handler = Handler(Looper.getMainLooper())

        // Loop over each question ID and create a notification for each one
        for (i in questionIds.indices) {
            handler.postDelayed({
                val builder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(context.getString(questionIds[i]))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent) // fire this intent when the user taps the notification
                    .setAutoCancel(true) // remove the notification when the user taps it

                val notificationManager = NotificationManagerCompat.from(context)
                val permissionGranted = ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                log("permissionGranted: $permissionGranted")

                notificationManager.notify(questionIds[i], builder.build())
            }, i * 60000L) // 60000L represents the delay in milliseconds (1 minute in this case)
        }
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(this.javaClass.simpleName, msg)
    }

}