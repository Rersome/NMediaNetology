package ru.netology.nmedia.dto

import kotlin.math.floor
import kotlin.math.round

object CalculateValues {

    fun calculateNumber(number: Int): String {
        return when {
            number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
            number >= 10_000 -> "${number / 1000}K"
            number >= 1_000 -> {
                val firstDigit = number / 1000
                val secondDigit = (number % 1000) / 100
                "$firstDigit.$secondDigit" + "K"
                //String.format("%.1fK", round(number / 1_000.0))
            }
            else -> number.toString()
        }
    }
}