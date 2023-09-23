package com.kanbancoders.mangatopia.scraper.services

import com.kanbancoders.mangatopia.scraper.common.BaseResponse
import com.kanbancoders.mangatopia.scraper.components.Manga
import com.kanbancoders.mangatopia.scraper.components.MangaRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MangaService @Autowired constructor(
    private val mangaRepository: MangaRepository,
) {

    fun getAll(): ResponseEntity<BaseResponse<List<Manga>>> {
        return BaseResponse.ok(mangaRepository.findAllByOrderByUpdatedAtDesc())
    }

    @Transactional
    fun deleteAll(): ResponseEntity<BaseResponse<List<Manga>>> {
        val deletedList = mangaRepository.findAll(
            Sort.by(Sort.Order.desc("updatedAt"))
        ).toList()
        mangaRepository.deleteAll()
        return BaseResponse.ok(deletedList)
    }

}