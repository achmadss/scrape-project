package com.kanbancoders.mangatopia.scraper.components

import jakarta.persistence.PostPersist

class MangaEntityListener {

    @PostPersist
    fun logPersistResult(manga: Manga) {
        println("\n$manga\n")
    }

}