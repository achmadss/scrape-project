package com.kanbancoders.mangatopia.scraper.components

import jakarta.persistence.Embeddable
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Embeddable
data class Chapter(
    val id: String? = null,
    val title: String = "",
    val timestamp: String = "",
    val imageUrls: MutableList<String> = mutableListOf(),
)
