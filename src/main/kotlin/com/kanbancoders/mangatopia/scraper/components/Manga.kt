package com.kanbancoders.mangatopia.scraper.components

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

@Entity
@Table
@EntityListeners(MangaEntityListener::class)
data class Manga(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    @Column(columnDefinition = "text")
    val title: String = "",
    @Column(columnDefinition = "text")
    val bannerUrl: String = "",
    @Column(columnDefinition = "text")
    val synopsis: String = "",
    val status: String = "",
    val type: String = "",
    val author: String = "",
    val artist: String = "",
    val genres: MutableList<String> = mutableListOf(),
    @ElementCollection
    @CollectionTable(name = "manga_chapters", joinColumns = [JoinColumn(name = "manga_id")])
    val chapters: MutableList<Chapter> = mutableListOf()
)
