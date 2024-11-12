package ru.netology.nmedia.dto

// Когда-нибудь пригодится

object FindYoutubeLink {

    fun extractYouTubeLink(text: String): String? {
        val pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/)[^#\\&\\?]*".toRegex()
        val matchResult = pattern.find(text)
        return matchResult?.value
    }
}