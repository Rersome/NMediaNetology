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
        dao.likeById(id)

        try {
            val response = PostApi.service.likeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: ApiError) {
            dao.unlikeById(id)
            throw e
        } catch (_: Exception) {
            dao.unlikeById(id)
            throw UnknownError
        } catch (_: IOException) {
            dao.unlikeById(id)
            throw NetworkError
        }
    }

    override suspend fun unlikeById(id: Long) {
        dao.unlikeById(id)

        try {
            val response = PostApi.service.unlikeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: ApiError) {
            dao.likeById(id)
            throw e
        } catch (_: Exception) {
            dao.likeById(id)
            throw UnknownError
        } catch (_: IOException) {
            dao.likeById(id)
            throw NetworkError
        }
    }

    override suspend fun shareById(id: Long) {
        dao.shareById(id)

        try {
            val response = PostApi.service.shareById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: ApiError) {
            throw e
        } catch (_: Exception) {
            throw UnknownError
        } catch (_: IOException) {
            throw NetworkError
        }
    }

    override suspend fun removeById(id: Long) {
        dao.removeById(id)

        try {
            val response = PostApi.service.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: ApiError) {
            throw e
        } catch (_: Exception) {
            throw UnknownError
        } catch (_: IOException) {
            throw NetworkError
        }
    }

    override suspend fun save(post: Post) {
        dao.save(PostEntity.fromDto(post))

        try {
            val response = PostApi.service.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: ApiError) {
            throw e
        } catch (_: Exception) {
            throw UnknownError
        } catch (_: IOException) {
            throw NetworkError
        }
    }
}