package ru.netology.nmedia.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryRoomImpl
import ru.netology.nmedia.util.SingleLiveEvent
import kotlin.concurrent.thread

private val empty = Post(
    id = 0L,
    likes = 0L,
    reposts = 0L,
    author = "",
    content = "",
    published = 0L, //TODO переделать
    likedByMe = false,
    video = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryRoomImpl()
    private val _data = MutableLiveData(FeedState())
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit> = _postCreated

    val data: LiveData<FeedState> = _data
    val edited = MutableLiveData(empty)
    private var isEditingCanceled = false

    init {
        load()
    }

    fun applyChangeAndSave(newText: String) {
        Log.d("MyTag", "Значение result: $isEditingCanceled")
        if (isEditingCanceled) {
            isEditingCanceled = false
        }
        edited.value?.let {
            thread {
                val text = newText.trim()
                if (text.isNotEmpty() && text != it.content) {
                    repository.save(it.copy(content = text, published = it.published))
                    _postCreated.postValue(Unit)
                    edited.postValue(empty)
                }
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun load() {
        thread {
            _data.postValue(FeedState(loading = true))

            val state = try {
                val posts = repository.getAll()

                FeedState(posts = posts, empty = posts.isEmpty())
            } catch (e: Exception) {
                FeedState(error = true)
            }
            _data.postValue(state)
        }
    }

    fun cancelEdit() {
        isEditingCanceled = true
        edited.value = empty
        //Log.d("MyTag", "Значение result: $isEditingCanceled")
    }

    fun likeById(id: Long) {
        val currentState = _data.value ?: return
        thread {
            _data.postValue(
                currentState.copy(
                    posts = currentState.posts.map {if (it.id != id) it else it.copy(likedByMe = !it.likedByMe, video = "", likes = it.likes + if (it.likedByMe) -1 else 1)}
                )
            )
            try {
                if (!currentState.posts.find { it.id == id }!!.likedByMe) {
                    repository.likeById(id)
                } else {
                    repository.unlikeById(id)
                }
            } catch (e: Exception) {
                _data.postValue(currentState)
            }
        }
    }

    fun shareById(id: Long) {
        val currentState = _data.value ?: return
        thread {
            _data.postValue(
                currentState.copy(
                    posts = currentState.posts.map { if (it.id != id) it else it.copy(reposts = it.reposts + 1, video = "") }
                )
            )
            try {
                repository.shareById(id)
            } catch (e: Exception) {
                _data.postValue(currentState)
            }
        }
    }

    fun removeById(id: Long) {
        val currentState = _data.value ?: return
        thread {
            _data.postValue(
                currentState.copy(
                    posts = currentState.posts.filter { it.id != id }
                )
            )
            try {
                repository.removeById(id)
            } catch (e: Exception) {
                _data.postValue(currentState)
            }
        }
    }
}