package ru.netology.nmedia.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.Api
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class AuthViewModel : ViewModel() {
    val data = AppAuth.getInstance().authState.asLiveData()
    val authenticationState = MutableLiveData<Result<Unit>?>()

    val isAuthorized: Boolean
        get() = AppAuth.getInstance().authState.value != null

    fun authenticateUser(login: String, pass: String) = viewModelScope.launch {
        try {
            val response = Api.service.updateUser(login, pass)

            if (response.isSuccessful) {
                response.body()?.let { token ->
                    AppAuth.getInstance().setAuth(token)
                    authenticationState.value = Result.success(Unit)
                } ?: run {
                    authenticationState.value =
                        Result.failure(Exception("Неправильный логин или пароль"))
                }
            } else {
                authenticationState.value =
                    Result.failure(ApiError(response.code(), response.message()))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: ApiError) {
            authenticationState.value = Result.failure(e)
        } catch (_: Exception) {
            authenticationState.value = Result.failure(UnknownError)
        } catch (_: IOException) {
            authenticationState.value = Result.failure(NetworkError)
        }
    }
}