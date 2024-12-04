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
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {

    private val channelId = "Notifications"
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val action = message.data["action"] ?: return

        try {
            when (ACTION.valueOf(action)) {
                ACTION.LIKE -> {
                    val content = message.data["content"]
                    val like = gson.fromJson<Like>(content, Like::class.java)
                    handleLike(like)
                }
                ACTION.SHARE -> {
                    val content = message.data["content"]
                    val share = gson.fromJson<Share>(content, Share::class.java)
                    handleShare(share)
                }
                ACTION.NEWPOST -> {
                    val content = message.data["content"]
                    val newPost = gson.fromJson<NewPost>(content, NewPost::class.java)
                    handleNewPost(newPost)
                }
            }
        } catch (e: IllegalArgumentException) {
            Log.d("Unknown action", action)
        }
    }

    override fun onNewToken(token: String) {
        Log.d("token", token)
        println(token)
    }

    private fun handleLike(like: Like) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_24dp)
            .setContentTitle(getString(R.string.like_notification, like.userName, like.postAuthor))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Random.nextInt(100_000), notification)
    }

    private fun handleShare(share: Share) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_24dp)
            .setContentTitle(getString(R.string.share_notification, share.userName, share.postAuthor))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Random.nextInt(100_000), notification)
    }

    @SuppressLint("StringFormatMatches")
    private fun handleNewPost(newPost: NewPost) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_24dp)
            .setContentTitle(getString(R.string.newPost_notification, newPost.userName, newPost.postAuthor))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(newPost.postText))
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Random.nextInt(100_000), notification)
    }
}

enum class ACTION {
    LIKE,
    SHARE,
    NEWPOST
}

data class Like(
    val userId: Long,
    val userName: String,
    val postAuthor: String,
    val postId: Long
)

data class Share(
    val userId: Long,
    val userName: String,
    val postAuthor: String,
    val postId: Long
)

data class NewPost(
    val userId: Long,
    val userName: String,
    val postAuthor: String,
    val postId: Long,
    val postText: String
)