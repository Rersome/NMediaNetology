package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {

    override val data: LiveData<List<Post>> = dao.getAll().map {
        it.toDto()
    }

    override suspend fun getAll() {
        try {
            val response = PostApi.service.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val posts = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(posts.toEntity())
        } catch (e: ApiError) {
            throw e
        } catch (_: Exception) {
            throw UnknownError
        } catch (_: IOException) {
            throw NetworkError
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            val response = PostApi.service.likeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val post = response.body() ?: throw ApiError(response.code(), response.message())
            dao.likeById(id)
        } catch (e: ApiError) {
            throw e
        } catch (_: Exception) {
            throw UnknownError
        } catch (_: IOException) {
            throw NetworkError
        }
    }

    override suspend fun unlikeById(id: Long) {
        try {
            val response = PostApi.service.unLikeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val post = response.body() ?: throw ApiError(response.code(), response.message())
            dao.unLikeById(id)
        } catch (e: ApiError) {
            throw e
        } catch (_: Exception) {
            throw UnknownError
        } catch (_: IOException) {
            throw NetworkError
        }
    }

    override suspend fun shareById(id: Long) {
        try {
            val response = PostApi.service.shareById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            dao.shareById(id)
        } catch (e: ApiError) {
            throw e
        } catch (_: Exception) {
            throw UnknownError
        } catch (_: IOException) {
            throw NetworkError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            val response = PostApi.service.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            dao.removeById(id)
        } catch (e: ApiError) {
            throw e
        } catch (_: Exception) {
            throw UnknownError
        } catch (_: IOException) {
            throw NetworkError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val response = PostApi.service.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val post = response.body() ?: throw ApiError(response.code(), response.message())
            dao.save(PostEntity.fromDto(post))
        } catch (e: ApiError) {
            throw e
        } catch (_: Exception) {
            throw UnknownError
        } catch (_: IOException) {
            throw NetworkError
        }
    }
}