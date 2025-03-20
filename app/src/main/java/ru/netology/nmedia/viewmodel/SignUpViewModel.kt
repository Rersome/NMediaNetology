package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.PhotoModel
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val apiService: ApiService
) : ViewModel() {
    val data = appAuth.authState.asLiveData()
    val signUpState = MutableLiveData<Result<Unit>?>()

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?> = _photo

    fun registerUser(name: String, login: String, pass: String) = viewModelScope.launch {
        try {
            val response = apiService.registerUser(login, pass, name)

            if (response.isSuccessful) {
                response.body()?.let { token ->
                    appAuth.setAuth(token)
                    signUpState.value = Result.success(Unit)
                } ?: run {
                    signUpState.value =
                        Result.failure(Exception("Неправильный логин или пароль"))
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: ApiError) {
            signUpState.value = Result.failure(e)
        } catch (_: Exception) {
            signUpState.value = Result.failure(UnknownError)
        } catch (_: IOException) {
            signUpState.value = Result.failure(NetworkError)
        }
    }

    fun registerUserWithPhoto(name: String, login: String, pass: String, photo: PhotoModel) =
        viewModelScope.launch {
            try {
                val response = apiService.registerWithPhoto(
                    login.toRequestBody("text/plain".toMediaType()),
                    pass.toRequestBody("text/plain".toMediaType()),
                    name.toRequestBody("text/plain".toMediaType()),
                    photo.let {
                        MultipartBody.Part.createFormData(
                            "file", it.file.name, it.file.asRequestBody()
                        )
                    }
                )

                if (response.isSuccessful) {
                    response.body()?.let { token ->
                        appAuth.setAuth(token)
                        signUpState.value = Result.success(Unit)
                    } ?: run {
                        signUpState.value =
                            Result.failure(Exception("Неправильный логин или пароль"))
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiError) {
                signUpState.value = Result.failure(e)
            } catch (_: Exception) {
                signUpState.value = Result.failure(UnknownError)
            } catch (_: IOException) {
                signUpState.value = Result.failure(NetworkError)
            }
        }

    fun savePhoto(photoModel: PhotoModel?) {
        _photo.value = photoModel
    }
}