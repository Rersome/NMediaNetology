package ru.netology.nmedia.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0L,
    likes = 0L,
    reposts = 0L,
    author = "",
    content = "",
    published = 0L, //TODO переделать
    likedByMe = false
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit> = _postCreated

    val data: LiveData<FeedModel> = _data
    val edited = MutableLiveData(empty)
    private var isEditingCanceled = false

    init {
        loadPosts()
    }

    fun applyChangeAndSave(newText: String) {
        Log.d("MyTag", "Значение result: $isEditingCanceled")
        if (isEditingCanceled) {
            isEditingCanceled = false
        }
        edited.value?.let {
            val text = newText.trim()
            if (text.isNotEmpty() && text != it.content) {
                repository.save(
                    it.copy(content = text),
                    object : PostRepository.PostCallback<Post> {
                        override fun onSuccess(result: Post) {
                            loadPosts()
                        }

                        override fun onError(error: Throwable) {
                            _data.value = FeedModel(error = true)
                        }
                    }
                )
                _postCreated.postValue(Unit)
                edited.postValue(empty)
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)

        repository.getAllAsync(
            object : PostRepository.PostCallback<List<Post>> {
                override fun onSuccess(result: List<Post>) {
                    _data.value = FeedModel(posts = result, empty = result.isEmpty())
                }

                override fun onError(error: Throwable) {
                    _data.value = FeedModel(error = true)
                }
            }
        )
    }

    fun cancelEdit() {
        isEditingCanceled = true
        edited.value = empty
    }

    fun likeById(id: Long) {
        val currentState = _data.value ?: return
        _data.value =
            currentState.copy(
                posts = currentState.posts.map {
                    if (it.id != id) it else
                        it.copy(
                            likedByMe = !it.likedByMe,
                            likes = it.likes + if (it.likedByMe) -1 else 1
                        )
                }
            )
        if (!currentState.posts.find { it.id == id }!!.likedByMe) {
            repository.likeById(id, object : PostRepository.PostCallback<Post> {
                override fun onSuccess(result: Post) {

                }

                override fun onError(error: Throwable) {
                    _data.value = FeedModel(error = true)
                }
            })
        } else {
            repository.unlikeById(id, object : PostRepository.PostCallback<Post> {
                override fun onSuccess(result: Post) {

                }

                override fun onError(error: Throwable) {
                    _data.value = FeedModel(error = true)
                }
            })
        }
    }

    fun shareById(id: Long) {
        val currentState = _data.value ?: return
        _data.value =
            currentState.copy(
                posts = currentState.posts.map {
                    if (it.id != id) it else it.copy(
                        reposts = it.reposts + 1,
                    )
                }
            )
        repository.shareById(id, object : PostRepository.PostCallback<Post> {
            override fun onSuccess(result: Post) {

            }

            override fun onError(error: Throwable) {
                _data.value = FeedModel(error = true)
            }
        })
    }

    fun removeById(id: Long) {
        val currentState = _data.value ?: return
        _data.value =
            currentState.copy(
                posts = currentState.posts.filter { it.id != id }
            )
        repository.removeById(id, object : PostRepository.PostCallback<Unit> {
            override fun onSuccess(result: Unit) {

            }

            override fun onError(error: Throwable) {
                _data.value = FeedModel(error = true)
            }
        })
    }
}