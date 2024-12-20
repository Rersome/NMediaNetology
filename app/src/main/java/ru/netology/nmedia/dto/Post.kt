package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: Long,
    val likes: Long = 0,
    val reposts: Long = 0,
    val likedByMe: Boolean = false,
    val video: String = ""
)