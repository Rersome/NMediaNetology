package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.FeedError
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject

private val empty = Post(
    id = 0L,
    likes = 0L,
    reposts = 0L,
    author = "",
    content = "",
    published = 0L,
    likedByMe = false,
    authorId = 0L
)

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,
    private val appDb: AppDb
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<FeedItem>> =
        appAuth.authState.flatMapLatest { token ->
            repository.data
                .map {
                    it.map { post ->
                        if (post is Post) {
                            post.copy(ownedByMe = post.authorId == token?.id)
                        } else {
                            post
                        }
                    }
                }
        }
            .flowOn(Dispatchers.Default)

//    val newerCount: LiveData<Int> = data.switchMap {
//        val newerPostId = it.posts.firstOrNull()?.id ?: 0L
//
//        repository.getNewerCount(newerPostId).asLiveData()
//    }

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(empty)
    private var isEditingCanceled = false

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?> = _photo


    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: AppError) {
            when (e) {
                is ApiError -> _dataState.value = FeedModelState(FeedError.API)
                is NetworkError -> _dataState.value = FeedModelState(FeedError.NETWORK)
                is UnknownError -> _dataState.value = FeedModelState(FeedError.UNKNOWN)
            }
        }
    }

    fun showNewPosts() = viewModelScope.launch {
        try {
            repository.readAll()
        } catch (e: AppError) {
            when (e) {
                is ApiError -> _dataState.value = FeedModelState(FeedError.API)
                is NetworkError -> _dataState.value = FeedModelState(FeedError.NETWORK)
                is UnknownError -> _dataState.value = FeedModelState(FeedError.UNKNOWN)
            }
        }
    }

    fun applyChangeAndSave(newText: String) = viewModelScope.launch {
        try {
            if (isEditingCanceled) {
                isEditingCanceled = false
            }
            edited.value?.let {
                val text = newText.trim()
                if (text.isNotEmpty() && text != it.content) {
                    _photo.value?.let { postWithAttachment ->
                        repository.saveWithAttachment(it.copy(content = text), postWithAttachment)
                    } ?: repository.save(it.copy(content = text))
                    _postCreated.postValue(Unit)
                }
            }
            edited.value = empty
        } catch (e: AppError) {
            when (e) {
                is ApiError -> _dataState.value = FeedModelState(FeedError.API)
                is NetworkError -> _dataState.value = FeedModelState(FeedError.NETWORK)
                is UnknownError -> _dataState.value = FeedModelState(FeedError.UNKNOWN)
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun cancelEdit() {
        isEditingCanceled = true
        edited.value = empty
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            appDb.postDao().getById(id).firstOrNull()?.map {
                it.copy(
                    likedByMe = !it.likedByMe,
                    likes = it.likes + if (it.likedByMe) -1 else 1)
                if (it.likedByMe) {
                    repository.unlikeById(id)
                } else {
                    repository.likeById(id)
                }
            }
        } catch (e: AppError) {
            when (e) {
                is ApiError -> _dataState.value = FeedModelState(FeedError.API)
                is NetworkError -> _dataState.value = FeedModelState(FeedError.NETWORK)
                is UnknownError -> _dataState.value = FeedModelState(FeedError.UNKNOWN)
            }
        }
    }

    fun shareById(id: Long) = viewModelScope.launch {
        edited.value?.let {
            if (it.id != id) it else it.copy(
                reposts = it.reposts + 1
            )
        }
        repository.shareById(id)
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            val post = appDb.postDao().getById(id).firstOrNull()
            post.let {
                repository.removeById(id)
            }
        } catch (e: AppError) {
            when (e) {
                is ApiError -> _dataState.value = FeedModelState(FeedError.API)
                is NetworkError -> _dataState.value = FeedModelState(FeedError.NETWORK)
                is UnknownError -> _dataState.value = FeedModelState(FeedError.UNKNOWN)
            }
        }
    }

    fun savePhoto(photoModel: PhotoModel?) {
        _photo.value = photoModel
    }
}