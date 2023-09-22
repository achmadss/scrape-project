package com.kanbancoders.mangatopia.scraper.components

import jakarta.persistence.PostPersist
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.LocalDateTime

class MangaEntityListener {

    @PrePersist
    fun onPrePersist(manga: Manga) {
        manga.updatedAt = LocalDateTime.now()
    }

    @PostPersist
    fun onPostPersist(manga: Manga) {
        println("${manga.title}: ${manga.link}")
    }

    @PreUpdate
    fun onPreUpdate(manga: Manga) {
        println("update manga: ${manga.title}")
        manga.updatedAt = LocalDateTime.now()
    }

}