package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM PostEntity WHERE visible = 1 ORDER BY id DESC")
    fun getAllVisible(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("SELECT COUNT(*) FROM PostEntity WHERE visible = 0")
    fun hiddenPostsCount(): Flow<Int>

    @Query("UPDATE PostEntity SET content = :content WHERE id = :postId")
    suspend fun changeContent(postId: Long, content: String)

    suspend fun save(post: PostEntity) = if (post.id != 0L) changeContent(post.id, post.content) else insert(post)

    @Query("""
        UPDATE PostEntity SET
               reposts = reposts + 1
           WHERE id = :id;
    """)
    suspend fun shareById(id: Long)

    @Query("""
        UPDATE PostEntity SET
               likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
               likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
           WHERE id = :id;
    """)
    suspend fun likeById(id: Long)

    @Query("""
        UPDATE PostEntity SET
               likes = likes -1,
               likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
           WHERE id = :id;
    """)
    suspend fun unlikeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("UPDATE PostEntity SET visible = 1")
    suspend fun readAll()

    @Query("DELETE FROM PostEntity")
    suspend fun clear()
}