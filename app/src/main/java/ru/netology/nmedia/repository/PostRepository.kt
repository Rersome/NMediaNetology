package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAllAsync(callBack: PostCallback<List<Post>>)
    fun likeById(id: Long, callBack: PostCallback<Post>)
    fun unlikeById(id: Long, callBack: PostCallback<Post>)
    fun shareById(id: Long, callBack: PostCallback<Post>)
    fun removeById(id: Long, callBack: PostCallback<Unit>)
    fun save(post: Post, callBack: PostCallback<Post>)

    interface PostCallback<T> {
        fun onSuccess(result: T)
        fun onError(error: Throwable)
    }
}