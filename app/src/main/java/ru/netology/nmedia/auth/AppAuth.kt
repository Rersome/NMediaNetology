package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.Api
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token
import kotlin.coroutines.EmptyCoroutineContext

class AppAuth private constructor(context: Context) {

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

    fun sendPushToken(token: String? = null) {
        CoroutineScope(EmptyCoroutineContext).launch {
            try {
                Api.service.savePushToken(
                    PushToken(token ?: FirebaseMessaging.getInstance().token.await())
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val TOKEN_KEY = "TOKEN_KEY"
        const val ID_KEY = "ID_KEY"

        @Volatile
        private var INSTANCE: AppAuth? = null

        fun getInstance(): AppAuth = requireNotNull(INSTANCE) {
            "You should call function initApp(context) before"
        }

        fun initApp(context: Context) {
            INSTANCE = AppAuth(context.applicationContext)
        }
    }
}