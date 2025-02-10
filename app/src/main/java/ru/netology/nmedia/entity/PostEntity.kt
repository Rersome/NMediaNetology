package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val likes: Long = 0,
    val reposts: Long = 0,
    val likedByMe: Boolean = false,
)

{
    fun toDto() = Post(
        id = id,
        author = author,
        authorAvatar = authorAvatar,
        content = content,
        published = published,
        likes = likes,
        reposts = reposts,
        likedByMe = likedByMe,
    )

    companion object {
        fun fromDto(post: Post) = PostEntity(
            id = post.id,
            author = post.author,
            authorAvatar = post.authorAvatar.toString(),
            content = post.content,
            published = post.published,
            likes = post.likes,
            reposts = post.reposts,
            likedByMe = post.likedByMe,
        )
    }
}

fun List<Post>.toEntity() = map {
    PostEntity.fromDto(it)
}

fun List<PostEntity>.toDto() = map {
    it.toDto()
}