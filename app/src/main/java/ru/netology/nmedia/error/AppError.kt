package ru.netology.nmedia.error

sealed class AppError(val text: String): Exception()
class ApiError(val status: Int, msg: String): AppError(msg)
object NetworkError: AppError("Network error")
object UnknownError: AppError("Unknown error")