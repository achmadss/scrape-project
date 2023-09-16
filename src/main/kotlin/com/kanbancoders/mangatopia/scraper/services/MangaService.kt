package com.kanbancoders.mangatopia.scraper.services

import com.kanbancoders.mangatopia.scraper.common.BaseResponse
import com.kanbancoders.mangatopia.scraper.components.Manga
import com.kanbancoders.mangatopia.scraper.components.MangaRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class MangaService @Autowired constructor(
    private val mangaRepository: MangaRepository,
) {

    fun getAll(): ResponseEntity<BaseResponse<List<Manga>>> {
        return BaseResponse.ok(mangaRepository.findAll().toList())
    }

}