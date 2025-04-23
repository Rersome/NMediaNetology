package ru.netology.nmedia.dto

sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
    val author: String,
    val authorId: Long,
    val content: String,
    val published: Long,
    val likes: Long = 0,
    val reposts: Long = 0,
    val likedByMe: Boolean = false,
    val authorAvatar: String? = null,
    val attachment: Attachment? = null,
    val visible: Boolean = true,
    val ownedByMe: Boolean = false
) : FeedItem

data class Ad(
    override val id: Long,
    val image: String
) : FeedItem

data class Attachment(
    val url: String,
    val type: AttachmentType
)