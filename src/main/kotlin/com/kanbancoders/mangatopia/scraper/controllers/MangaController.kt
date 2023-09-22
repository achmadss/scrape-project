package com.kanbancoders.mangatopia.scraper.controllers

import com.kanbancoders.mangatopia.scraper.common.BaseResponse
import com.kanbancoders.mangatopia.scraper.components.Manga
import com.kanbancoders.mangatopia.scraper.services.MangaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["manga"])
@CrossOrigin(origins = ["*"])
class MangaController @Autowired constructor(
    private val mangaService: MangaService,
) {

    @GetMapping
    fun getAllMangas(): ResponseEntity<BaseResponse<List<Manga>>> = mangaService.getAll()

    @DeleteMapping
    fun deleteAllMangas(): ResponseEntity<BaseResponse<List<Manga>>> = mangaService.deleteAll()

}