package com.kanbancoders.mangatopia.scraper.components

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository

interface MangaRepository: PagingAndSortingRepository<Manga, String>, CrudRepository<Manga, String> {



}