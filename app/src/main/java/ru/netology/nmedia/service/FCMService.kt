package ru.netology.nmedia.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {

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
        AppAuth.getInstance().sendPushToken(token)
    }

//    private fun handleLike(like: Like) {
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification_24dp)
//            .setContentTitle(
//                getString(
//                    R.string.like_notification,
//                    like.userName,
//                    like.postAuthor
//                )
//            )
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//
//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(Random.nextInt(100_000), notification)
//    }
//
//    private fun handleShare(share: Share) {
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification_24dp)
//            .setContentTitle(
//                getString(
//                    R.string.share_notification,
//                    share.userName,
//                    share.postAuthor
//                )
//            )
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//
//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(Random.nextInt(100_000), notification)
//    }
//
//    @SuppressLint("StringFormatMatches")
//    private fun handleNewPost(newPost: NewPost) {
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification_24dp)
//            .setContentTitle(
//                getString(
//                    R.string.newPost_notification,
//                    newPost.userName,
//                    newPost.postAuthor
//                )
//            )
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setStyle(
//                NotificationCompat.BigTextStyle()
//                    .bigText(newPost.postText)
//            )
//            .build()
//
//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(Random.nextInt(100_000), notification)
//    }

    private fun pushMessage(pushMessage: PushMessage) {
        val recipientId = pushMessage.recipientId?.toString()
        val currentUserId = AppAuth.getInstance().authState.value?.id?.toString()

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
                    AppAuth.getInstance().sendPushToken()
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

//enum class ACTION {
//    LIKE,
//    SHARE,
//    NEWPOST,
//    PUSH
//}

//data class Like(
//    val userId: Long,
//    val userName: String,
//    val postAuthor: String,
//    val postId: Long
//)
//
//data class Share(
//    val userId: Long,
//    val userName: String,
//    val postAuthor: String,
//    val postId: Long
//)
//
//data class NewPost(
//    val userId: Long,
//    val userName: String,
//    val postAuthor: String,
//    val postId: Long,
//    val postText: String
//)

data class PushMessage(
    val recipientId: Long?,
    val content: String
)