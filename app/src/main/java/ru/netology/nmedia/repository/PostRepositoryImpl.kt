package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post

class PostRepositoryImpl : PostRepository {


    override fun save(post: Post, callBack: PostRepository.PostCallback<Post>) {
        PostApi.service.save(post)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        val errorMessage = response.errorBody()?.string()
                            ?: "Неизвестная ошибка"
                        callBack.onError(RuntimeException("Неполучилось сохранить пост: $errorMessage"))
                    } else {
                        val body = response.body() ?: throw RuntimeException("body is null")
                        callBack.onSuccess(body)
                    }
                }

                override fun onFailure(call: Call<Post>, e: Throwable) {
                    callBack.onError(e)
                }
            })
    }

    override fun getAllAsync(callBack: PostRepository.PostCallback<List<Post>>) {
        PostApi.service.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                    if (!response.isSuccessful) {
                        val errorMessage = response.errorBody()?.string()
                            ?: "Неизвестная ошибка"
                        callBack.onError(RuntimeException("Неполучилось получить список постов: $errorMessage"))
                    } else {
                        val body = response.body() ?: throw RuntimeException("body is null")
                        callBack.onSuccess(body)
                    }
                }

                override fun onFailure(call: Call<List<Post>>, e: Throwable) {
                    callBack.onError(e)
                }
            })
    }

    override fun likeById(id: Long, callBack: PostRepository.PostCallback<Post>) {
        PostApi.service.likeById(id)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (!response.isSuccessful) {
                        val errorMessage = response.errorBody()?.string()
                            ?: "Неизвестная ошибка"
                        callBack.onError(RuntimeException("Неполучилось лайкнуть пост: $errorMessage"))
                    } else {
                        response.body() ?: throw RuntimeException("body is null")
                    }
                }

                override fun onFailure(call: Call<Unit>, e: Throwable) {
                    callBack.onError(e)
                }
            })

    }

    override fun unlikeById(id: Long, callBack: PostRepository.PostCallback<Post>) {
        PostApi.service.unLikeById(id)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (!response.isSuccessful) {
                        val errorMessage = response.errorBody()?.string()
                            ?: "Неизвестная ошибка"
                        callBack.onError(RuntimeException("Неполучилось убрать лайк у поста: $errorMessage"))
                    } else {
                        response.body() ?: throw RuntimeException("body is null")
                    }
                }

                override fun onFailure(call: Call<Unit>, e: Throwable) {
                    callBack.onError(e)
                }
            })
    }

    override fun shareById(id: Long, callBack: PostRepository.PostCallback<Post>) {
        PostApi.service.shareById(id)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (!response.isSuccessful) {
                        val errorMessage = response.errorBody()?.string()
                            ?: "Неизвестная ошибка"
                        callBack.onError(RuntimeException("Неполучилось поделиться постом: $errorMessage"))
                    } else {
                        response.body() ?: throw RuntimeException("body is null")
                    }
                }

                override fun onFailure(call: Call<Unit>, e: Throwable) {
                    callBack.onError(e)
                }
            })
    }

    override fun removeById(id: Long, callBack: PostRepository.PostCallback<Unit>) {
        PostApi.service.removeById(id)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (!response.isSuccessful) {
                        val errorMessage = response.errorBody()?.string()
                            ?: "Неизвестная ошибка"
                        callBack.onError(RuntimeException("Удалить пост: $errorMessage"))
                    } else {
                        response.body() ?: throw RuntimeException("body is null")
                    }
                }

                override fun onFailure(call: Call<Unit>, e: Throwable) {
                    callBack.onError(e)
                }
            })
    }
}