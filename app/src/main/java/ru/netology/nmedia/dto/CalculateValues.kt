package ru.netology.nmedia.dto

object CalculateValues {

    fun calculateNumber(number: Int): String {
        return when {
            number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
            number >= 10_000 -> "${number / 1000}K"
            number >= 1_100 -> String.format("%.1fK", number / 1_000.0)
            else -> number.toString()
        }
    }
}