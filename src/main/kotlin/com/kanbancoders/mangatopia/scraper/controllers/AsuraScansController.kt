package com.kanbancoders.mangatopia.scraper.controllers

import com.kanbancoders.mangatopia.scraper.common.BaseResponse
import com.kanbancoders.mangatopia.scraper.components.Manga
import com.kanbancoders.mangatopia.scraper.services.AsuraScansService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    path = ["asurascans"]
)
@CrossOrigin(origins = ["*"])
class AsuraScansController @Autowired constructor(
    private val asuraScansService: AsuraScansService
) {

    @GetMapping(
        path = ["/scrape"]
    )
    fun startScrape(): ResponseEntity<Any> {
        asuraScansService.scrape()
        return ResponseEntity.ok("OK")
    }

    @GetMapping
    fun getAllMangas(): ResponseEntity<BaseResponse<List<Manga>>> = asuraScansService.getAll()

}