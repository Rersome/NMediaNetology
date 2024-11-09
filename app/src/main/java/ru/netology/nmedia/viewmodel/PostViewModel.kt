package ru.netology.nmedia.viewmodel

import android.icu.util.Calendar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryInMemoryImpl

private val empty = Post(
    id = 0L,
    likes = 0L,
    reposts = 0L,
    author = "",
    content = "",
    published = Calendar.getInstance().time.toString(), //TODO переделать
    likedByMe = false,
)

class PostViewModel : ViewModel() {

    private val repository: PostRepository = PostRepositoryInMemoryImpl()

    val data = repository.getAll()
    val edited = MutableLiveData(empty)
    private var isEditingCanceled = false

    fun applyChangeAndSave(newText: String) {
        if (isEditingCanceled) {
            isEditingCanceled = false
        }
        edited.value?.let {
            val text = newText.trim()
            if (text.isNotEmpty() && text != it.content) {
                repository.save(it.copy(content = text))
            }
        }
        edited.value = empty
    }

    fun save() {
        edited.value?.let {
            repository.save(it)
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun cancelEdit() {
        isEditingCanceled = true
        edited.value = empty
    }

    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) = repository.removeById(id)

}