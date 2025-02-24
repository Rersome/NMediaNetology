package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: Long,
    val likes: Long = 0,
    val reposts: Long = 0,
    val likedByMe: Boolean = false,
    val authorAvatar: String? = null,
    val attachment: Attachment? = null,
    val visible: Boolean = true,
)

data class Attachment(
    val url: String,
    val type: AttachmentType
)