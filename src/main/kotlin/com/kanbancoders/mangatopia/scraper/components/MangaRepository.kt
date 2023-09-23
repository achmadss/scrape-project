package com.kanbancoders.mangatopia.scraper.components

import java.util.Optional
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository

interface MangaRepository: PagingAndSortingRepository<Manga, String>, CrudRepository<Manga, String> {

    fun findByTitleIgnoreCase(title: String): Optional<Manga>
    fun findAllByOrderByUpdatedAtDesc(): List<Manga>
    fun findTopByOrderByUpdatedAtDesc(): Optional<Manga>

}