package com.kanbancoders.mangatopia.scraper.components

import jakarta.persistence.Embeddable
import java.time.LocalDateTime

@Embeddable
data class Chapter(
    val title: String = "",
    val timestamp: LocalDateTime,
    val chapterUrl: String = "",
    val imageUrls: MutableList<String> = mutableListOf(),
)
