package com.kanbancoders.mangatopia.scraper.components

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table

@Entity
@Table
data class Chapter(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    val title: String = "",
    val timestamp: String = "",
    @Column(length = 1024)
    val imageUrls: MutableList<String> = mutableListOf(),
)
