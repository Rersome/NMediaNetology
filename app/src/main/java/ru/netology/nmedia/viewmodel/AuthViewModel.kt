package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.netology.nmedia.auth.AppAuth

class AuthViewModel: ViewModel() {
    val data = AppAuth.getInstance().authState.asLiveData()

    val isAuthorized: Boolean
        get() = AppAuth.getInstance().authState.value != null
}