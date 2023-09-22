package com.kanbancoders.mangatopia.scraper.components

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

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
    @ElementCollection(fetch = FetchType.EAGER)
    val chapters: MutableList<Chapter> = mutableListOf(),
    @Column(columnDefinition = "text")
    val link: String? = null,
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @UpdateTimestamp
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)
