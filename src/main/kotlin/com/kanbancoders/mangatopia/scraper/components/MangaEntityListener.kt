package com.kanbancoders.mangatopia.scraper.components

import jakarta.persistence.PostPersist
import jakarta.persistence.PostUpdate

class MangaEntityListener {

    @PostPersist
    fun onPostPersist(manga: Manga) {
        println("${manga.title}: ${manga.link}")
    }

    @PostUpdate
    fun onPostUpdate(manga: Manga) {
        println("updated manga: ${manga.title}")
    }

}