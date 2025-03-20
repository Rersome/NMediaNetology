package ru.netology.nmedia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var appAuth: AppAuth

    private val content = "content"
    private val channelId = "Notifications"
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        message.data[content]?.let {
            try {
                pushMessage(gson.fromJson(it, PushMessage::class.java))
            } catch (e: IllegalArgumentException) {
                Log.d("Unknown action", it)
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM token", token)
        appAuth.sendPushToken(token)
    }

    private fun pushMessage(pushMessage: PushMessage) {
        val recipientId = pushMessage.recipientId?.toString()
        val currentUserId = appAuth.authState.value?.id?.toString()

        when {
            recipientId == currentUserId -> {
                val notification = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_notification_24dp)
                    .setContentTitle(
                        getString(
                            R.string.share_notification,
                            pushMessage.recipientId.toString(),
                            pushMessage.content
                        )
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(Random.nextInt(100_000), notification)

            }

            recipientId == "0" -> {
                if (currentUserId != null) {
                    appAuth.sendPushToken()
                } else {
                    val notification = NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification_24dp)
                        .setContentTitle(
                            getString(
                                R.string.share_notification,
                                recipientId,
                                pushMessage.content
                            )
                        )
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()

                    val notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(Random.nextInt(100_000), notification)
                }

            }

            recipientId != "0" -> {
                val notification = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_notification_24dp)
                    .setContentTitle(
                        getString(
                            R.string.share_notification,
                            pushMessage.recipientId.toString(),
                            pushMessage.content
                        )
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(Random.nextInt(100_000), notification)
            }
        }
    }
}

data class PushMessage(
    val recipientId: Long?,
    val content: String
)