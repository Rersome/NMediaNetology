package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.util.concurrent.TimeUnit

class PostRepositoryImpl : PostRepository {

    private val client = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private val typeToken = object : TypeToken<List<Post>>() {

    }

    private companion object {
        const val BASE_URL = "http://10.0.2.2:9999/"
        val jsonType = "application/json".toMediaType()
    }

    override fun save(post: Post, callBack: PostRepository.PostCallback<Post>) {
        val request = Request.Builder()
            .url("${BASE_URL}api/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callBack.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body
                    if (body == null) {
                        callBack.onError(RuntimeException("Body is null"))
                        return
                    }
                    try {
                        callBack.onSuccess(
                            gson.fromJson(
                                body.string(),
                                Post::class.java
                            )
                        )
                    } catch (e: Exception) {
                        callBack.onError(e)
                    }
                }
            })
    }

    override fun getAllAsync(callBack: PostRepository.PostCallback<List<Post>>) {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callBack.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body
                    if (body == null) {
                        callBack.onError(RuntimeException("Body is null"))
                        return
                    }
                    try {
                        callBack.onSuccess(gson.fromJson(body.string(), typeToken.type))
                    } catch (e: Exception) {
                        callBack.onError(e)
                    }
                 }
            })
    }

    override fun likeById(id: Long, callBack: PostRepository.PostCallback<Post>) {
        val request = Request.Builder()
            .url("${BASE_URL}api/posts/$id/likes")
            .post(gson.toJson(id).toRequestBody(jsonType))
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callBack.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body
                    if (body == null) {
                        callBack.onError(RuntimeException("Body is null"))
                        return
                    }
                    try {
                        callBack.onSuccess(
                            gson.fromJson(
                                body.string(),
                                Post::class.java
                            )
                        )
                    } catch (e: Exception) {
                        callBack.onError(e)
                    }
                }
            })
    }

    override fun unlikeById(id: Long, callBack: PostRepository.PostCallback<Post>) {
        val request = Request.Builder()
            .url("${BASE_URL}api/posts/$id/likes")
            .delete(gson.toJson(id).toRequestBody(jsonType))
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callBack.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body
                    if (body == null) {
                        callBack.onError(RuntimeException("Body is null"))
                        return
                    }
                    try {
                        callBack.onSuccess(
                            gson.fromJson(
                                body.string(),
                                Post::class.java
                            )
                        )
                    } catch (e: Exception) {
                        callBack.onError(e)
                    }
                }
            })
    }

    override fun shareById(id: Long, callBack: PostRepository.PostCallback<Post>) {
        val request = Request.Builder()
            .url("${BASE_URL}api/posts/$id/reposts")
            .post(gson.toJson(id).toRequestBody(jsonType))
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callBack.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body
                    if (body == null) {
                        callBack.onError(RuntimeException("Body is null"))
                        return
                    }
                    try {
                        callBack.onSuccess(
                            gson.fromJson(
                                body.string(),
                                Post::class.java
                            )
                        )
                    } catch (e: Exception) {
                        callBack.onError(e)
                    }
                }
            })
    }

    override fun removeById(id: Long, callBack: PostRepository.PostCallback<Unit>) {
        val request = Request.Builder()
            .delete()
            .url("${BASE_URL}api/slow/posts/$id")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callBack.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.body == null) {
                        callBack.onError(RuntimeException("Body is null"))
                        return
                    }
                    try {
                        callBack.onSuccess(Unit)
                    } catch (e: Exception) {
                        callBack.onError(e)
                    }
                }
            })
    }
}