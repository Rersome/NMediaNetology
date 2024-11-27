package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likes: Long = 0,
    val reposts: Long = 0,
    val likedByMe: Boolean = false,
    val video: String
) {
    fun toDto() = Post(
        id,
        author,
        content,
        published,
        likes,
        reposts,
        likedByMe,
        video)

    companion object {
        fun fromDto(post: Post) = PostEntity(
            post.id,
            post.author,
            post.content,
            post.published,
            post.likes,
            post.reposts,
            post.likedByMe,
            post.video)
    }
}