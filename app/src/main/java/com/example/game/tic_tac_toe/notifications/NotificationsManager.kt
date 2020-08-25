package com.example.game.tic_tac_toe.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.game.controllers.GameID
import com.example.game.controllers.GameInitializer
import com.example.game.tic_tac_toe.GameApplication
import com.example.game.tic_tac_toe.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NotificationsManager(private val appContext: Context) {
    var shown: Boolean = false
        private set

    init {
        createNotificationChannel()
    }

    companion object {
        private const val GAME_AWAIT_CHANNEL = "TIC-TAC-TOE GAME AWAIT"
        private const val GAME_READY_CHANNEL = "TIC-TAC-TOE GAME READY"
        private const val notID = 1337
        private const val showDelay: Long = 100
    }

    private val pendingReturnIntent = PendingIntent.getActivity(
            appContext, 0,
            Intent(appContext, GameApplication::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_ONE_SHOT
    )


    private val cancelPendingIntent = PendingIntent.getBroadcast(
            appContext,
            1,
            Intent(NotificationReceiver.CANCEL_GAME),
            PendingIntent.FLAG_ONE_SHOT
    )

    private lateinit var builder: NotificationCompat.Builder

    fun setupNotification(initializer: GameInitializer) {
        val notReceiver = NotificationReceiver()
        notReceiver.registerCallback {
            initializer.cancelGame()
            NotificationManagerCompat.from(appContext).cancel(notID)
            appContext.unregisterReceiver(notReceiver)
            shown = false
        }
        val filter = IntentFilter(NotificationReceiver.CANCEL_GAME)
        appContext.registerReceiver(notReceiver, filter)

        builder = notTemplate(GAME_AWAIT_CHANNEL)
                .setProgress(0, 0, true)
                .addAction(android.R.drawable.ic_delete, "Cancel", cancelPendingIntent)
        GlobalScope.launch {
            delay(showDelay)
            with(NotificationManagerCompat.from(appContext)) {
                notify(notID, builder.build())
            }
            shown = true
        }

    }

    fun setupGameID(id: GameID) {
        builder.setContentText("Game ID: ${id.ID}")
        if (shown) {
            with(NotificationManagerCompat.from(appContext)) {
                notify(notID, builder.build())
            }
        }
    }

    fun creationFailed() {
        with(NotificationManagerCompat.from(appContext)) {
            notify(notID, notTemplate(GAME_AWAIT_CHANNEL).setContentText("Creation Fail").build())
        }
    }

    fun gameCreated() {
        val not = notTemplate(GAME_READY_CHANNEL)
                .setContentText("Opponent joined the game")
                .setContentIntent(pendingReturnIntent)
                .setAutoCancel(true)
                .build()
        with(NotificationManagerCompat.from(appContext)) {
            notify(notID, not)
        }
    }

    fun closeNotification() {
        with(NotificationManagerCompat.from(appContext)) {
            cancel(notID)
        }
        shown = false
    }

    private fun notTemplate(channelId: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(appContext, channelId)
                .setSmallIcon(R.mipmap.game_launcher)
                .setLargeIcon(ResourcesCompat.getDrawable(appContext.resources, R.mipmap.game_launcher, null)!!.toBitmap())
                .setContentTitle("Tic-Tac-Toe")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setOngoing(true)
                .setVibrate(longArrayOf(0))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val awaitName = "game await"
            val awaitDescription = "game awaiting notification"
            val awaitImportance = NotificationManager.IMPORTANCE_HIGH
            val gameAwaiting = NotificationChannel(GAME_AWAIT_CHANNEL, awaitName, awaitImportance).apply {
                description = awaitDescription
                setSound(null, null)
                enableVibration(true)
                vibrationPattern = longArrayOf(0)
            }

            val readyName = "game ready"
            val readyDescription = "game ready notification"
            val readyImportance = NotificationManager.IMPORTANCE_HIGH
            val readyAwaiting = NotificationChannel(GAME_READY_CHANNEL, readyName, readyImportance).apply {
                description = readyDescription
            }

            notificationManager.createNotificationChannels(listOf(gameAwaiting, readyAwaiting))
        }
    }
}