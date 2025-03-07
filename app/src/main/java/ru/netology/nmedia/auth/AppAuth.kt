package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nmedia.dto.Token

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
    }

    @Synchronized
    fun setAuth(token: Token) {
        prefs.edit {
            putLong(ID_KEY, token.id)
            putString(TOKEN_KEY, token.token)
        }
        _authState.value = token
    }

    @Synchronized
    fun clear() {
        prefs.edit { clear() }
        _authState.value = null
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