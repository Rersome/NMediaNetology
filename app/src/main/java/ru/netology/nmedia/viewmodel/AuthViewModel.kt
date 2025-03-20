package ru.netology.nmedia.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val apiService: ApiService
) : ViewModel() {
    val data = appAuth.authState.asLiveData()
    val authenticationState = MutableLiveData<Result<Unit>?>()

    val isAuthorized: Boolean
        get() = appAuth.authState.value != null

    fun authenticateUser(login: String, pass: String) = viewModelScope.launch {
        try {
            val response = apiService.updateUser(login, pass)

            if (response.isSuccessful) {
                response.body()?.let { token ->
                    appAuth.setAuth(token)
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