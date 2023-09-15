package com.kanbancoders.mangatopia.scraper.controllers

import com.kanbancoders.mangatopia.scraper.common.BaseResponse
import com.kanbancoders.mangatopia.scraper.components.Manga
import com.kanbancoders.mangatopia.scraper.services.AsuraScansService
import com.kanbancoders.mangatopia.scraper.workers.PlaywrightWorker
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
        path = ["/scrape-start"]
    )
    fun startScrape(): ResponseEntity<BaseResponse<String>> {
        return BaseResponse.ok(asuraScansService.scrape())
    }

    @GetMapping(
        path = ["/scrape-stop"]
    )
    fun stopScrape(): ResponseEntity<BaseResponse<String>> {
        val isStopped = PlaywrightWorker.stopAllWorkers()
        return if (isStopped) {
            BaseResponse.ok("All workers gracefully stopped")
        } else BaseResponse.internalServerError("Not all workers are stopped")
    }

    @GetMapping
    fun getAllMangas(): ResponseEntity<BaseResponse<List<Manga>>> = asuraScansService.getAll()



}