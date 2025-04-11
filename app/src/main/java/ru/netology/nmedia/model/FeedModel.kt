package ru.netology.nmedia.model

enum class FeedError {
    NONE, API, NETWORK, UNKNOWN
}

data class FeedModelState(
    val error: FeedError = FeedError.NONE,
    val loading: Boolean = false,
    val refreshing: Boolean = false
)