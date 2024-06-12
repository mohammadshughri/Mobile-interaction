package de.luh.hci.mi.experiencesampling

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import de.luh.hci.mi.experiencesampling.data.CouchDB
import de.luh.hci.mi.experiencesampling.data.Repository
import de.luh.hci.mi.experiencesampling.data.RepositoryImpl
import de.luh.hci.mi.experiencesampling.notifications.NotificationTrigger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

// Time between notifications in milliseconds.
const val samplingInterval = 10000L // milliseconds

// Unique identifier for experimenter. Used to distinguish between entries in database.
const val experimenter = "testingApp" // todo: invent your own experimenter ID

// The application instance exists as long as the app executes and is therefore used for app-wide
// data that is used in activities.
class ExperienceSampling : Application() {
    // officially: use Dagger-Hilt dependency injection, rather than manual dependency injection
    // https://developer.android.com/training/dependency-injection
    // https://developer.android.com/training/dependency-injection/manual

    private lateinit var remoteDb: CouchDB
    lateinit var repository: Repository
        private set

    // Coroutine scope for submitting data to server. Continues even when navigating away from UI.
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        remoteDb = CouchDB("https://couchdb.hci.uni-hannover.de/mobint/")
        repository = RepositoryImpl(applicationContext, remoteDb)
    }

    // Whether or not periodic experience sampling is on.
    val periodicSampling = mutableStateOf(false)

    // Schedules the next notification delay ms from now.
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleNextNotification(delay: Long) {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(this, NotificationTrigger::class.java)
        val pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_MUTABLE)
        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + delay,
            pi
        )
    }

    // Cancels any registered notification.
    fun cancelNotification() {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(this, NotificationTrigger::class.java)
        val pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_MUTABLE)
        am.cancel(pi)
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(this.javaClass.simpleName, msg)
    }
}