package ru.netology.nmedia.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {

    override val data = dao.getAllVisible().map {
        it.toDto()
    }

    override fun getNewerCount(newerId: Long): Flow<Int> = flow {
        while (true) {
            try {
                delay(10.seconds)

                val response = PostApi.service.getNewer(newerId)

                val posts = response.body() ?: throw ApiError(response.code(), response.message())
                dao.insert(posts.toEntity(isVisible = false))
                emit(posts.size)
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiError) {
                throw e
            } catch (_: Exception) {
                throw UnknownError
            } catch (_: IOException) {
                throw NetworkError
            }
        }
    }
        .flowOn(Dispatchers.Default)

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

    override suspend fun readAll() {
        dao.readAll()
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