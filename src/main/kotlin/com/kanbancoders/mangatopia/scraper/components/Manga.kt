package com.kanbancoders.mangatopia.scraper.components

import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table

@Entity
@Table
@EntityListeners(MangaEntityListener::class)
data class Manga(
    @Id
    @Column(unique = true)
    @Lob
    val title: String = "",
    @Lob
    val bannerUrl: String = "",
    @Lob
    val synopsis: String = "",
    val status: String = "",
    val type: String = "",
    val author: String = "",
    val artist: String = "",
    val genres: MutableList<String> = mutableListOf(),
    @ElementCollection
    val chapters: MutableList<Chapter> = mutableListOf()
)
