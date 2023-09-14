package com.kanbancoders.mangatopia.scraper.components

import jakarta.persistence.Embeddable

@Embeddable
data class Chapter(
    val title: String = "",
    val timestamp: String = "",
    val imageUrls: List<String> = listOf(),
)
