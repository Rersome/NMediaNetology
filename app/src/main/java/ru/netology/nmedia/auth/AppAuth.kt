package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private val TOKEN_KEY = "TOKEN_KEY"
    private val ID_KEY = "ID_KEY"
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _authState = MutableStateFlow<Token?>(null)
    val authState: StateFlow<Token?> = _authState.asStateFlow()

    init {
        val id = prefs.getLong(ID_KEY, 0L)
        val token = prefs.getString(TOKEN_KEY, null)

        if (id != 0L || token == null) {
            prefs.edit { clear() }
        } else {
            _authState.value = Token(id = id, token = token)
        }
        sendPushToken()
    }

    @Synchronized
    fun setAuth(token: Token) {
        prefs.edit {
            putLong(ID_KEY, token.id)
            putString(TOKEN_KEY, token.token)
        }
        _authState.value = token
        sendPushToken()
    }

    @Synchronized
    fun clear() {
        prefs.edit { clear() }
        _authState.value = null
        sendPushToken()
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun getApiService(): ApiService
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(EmptyCoroutineContext).launch {
            try {
                val entryPoint =
                    EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
                entryPoint.getApiService().savePushToken(
                    PushToken(token ?: FirebaseMessaging.getInstance().token.await())
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}