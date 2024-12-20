package ru.netology.nmedia.repository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

class PostRepositoryRoomImpl: PostRepository {

    private val client = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private val typeToken = object : TypeToken<List<Post>> () {
        
    }

    private companion object {
        const val BASE_URL = "http://10.0.2.2:9999/"
        val jsonType = "application/json".toMediaType()
    }

    override fun getAll(): List<Post> {
        val request = Request.Builder()
            .url("${BASE_URL}api/posts")
            .build()

        val call = client.newCall(request)

        val response = call.execute()

        val body = response.body ?: error("Body is null")

        return gson.fromJson(body.string(), typeToken)

    }

    override fun save(post: Post): Post {
        val request = Request.Builder()
            .url("${BASE_URL}api/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()

        val call = client.newCall(request)

        val response = call.execute()

        val body = response.body ?: error("Body is null")

        return gson.fromJson(body.string(), Post::class.java)
    }

    override fun likeById(id: Long): Post {
        val request = Request.Builder()
            .url("${BASE_URL}api/posts/$id/likes")
            .post(gson.toJson(id).toRequestBody(jsonType))
            .build()

        val call = client.newCall(request)

        val response = call.execute()

        val body = response.body ?: error("Body is null")

        return gson.fromJson(body.string(), Post::class.java)
    }

    override fun unlikeById(id: Long): Post {
        val request = Request.Builder()
            .url("${BASE_URL}api/posts/$id/likes")
            .delete(gson.toJson(id).toRequestBody(jsonType))
            .build()

        val call = client.newCall(request)

        val response = call.execute()

        val body = response.body ?: error("Body is null")

        return gson.fromJson(body.string(), Post::class.java)
    }

    override fun shareById(id: Long): Post {
        val request = Request.Builder()
            .url("${BASE_URL}api/posts/$id/reposts")
            .post(gson.toJson(id).toRequestBody(jsonType))
            .build()

        val call = client.newCall(request)

        val response = call.execute()

        val body = response.body ?: error("Body is null")

        return gson.fromJson(body.string(), Post::class.java)
    }

    override fun removeById(id: Long) {
        val request = Request.Builder()
            .delete()
            .url("${BASE_URL}api/slow/posts/$id")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }
}