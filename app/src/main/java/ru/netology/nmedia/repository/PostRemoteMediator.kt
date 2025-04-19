package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    val lastId = postRemoteKeyDao.max() ?: 0L
                    if (lastId == 0L) {
                        apiService.getLatest(state.config.pageSize)
                    } else {
                        apiService.getAfter(lastId, state.config.pageSize)
                    }
                }

                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getBefore(id, state.config.pageSize)
                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(true)
                }
            }
            if (!result.isSuccessful) {
                throw ApiError(result.code(), result.message())
            }
            val body = result.body() ?: throw ApiError(
                result.code(),
                result.message()
            )

            appDb.withTransaction {

                when (loadType) {

                    LoadType.REFRESH -> {
                        if (postRemoteKeyDao.max() == null) {
                            body.firstOrNull()?.let { first ->
                                postRemoteKeyDao.insert(
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.AFTER,
                                        first.id
                                    )
                                )
                            }
                            body.lastOrNull()?.let { last ->
                                postRemoteKeyDao.insert(
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.BEFORE,
                                        last.id
                                    )
                                )
                            }
                        } else {
                            body.firstOrNull()?.let { first ->
                                postRemoteKeyDao.insert(
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.AFTER,
                                        first.id
                                    )
                                )
                            }
                        }
                    }

                    LoadType.APPEND -> {
                        body.lastOrNull()?.let { last ->
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    last.id
                                )
                            )
                        }
                    }

                    LoadType.PREPEND -> {

                    }
                }

                postDao.insert(body.map(PostEntity::fromDto))
            }
            return MediatorResult.Success(body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}