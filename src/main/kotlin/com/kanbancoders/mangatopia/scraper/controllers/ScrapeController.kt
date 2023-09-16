package com.kanbancoders.mangatopia.scraper.controllers

import com.kanbancoders.mangatopia.scraper.common.BaseResponse
import com.kanbancoders.mangatopia.scraper.common.error.GeneralException
import com.kanbancoders.mangatopia.scraper.components.Manga
import com.kanbancoders.mangatopia.scraper.services.ScrapeService
import com.kanbancoders.mangatopia.scraper.workers.PlaywrightWorker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    path = ["scrape"]
)
@CrossOrigin(origins = ["*"])
class ScrapeController @Autowired constructor(
    private val scrapeService: ScrapeService,
) {

    // Start scraping using a strategy with a given number of workers
    @PostMapping("/start")
    fun startScraping(
        @RequestParam("strategyName") strategyName: String,
        @RequestParam("workerCount") workerCount: Int
    ): ResponseEntity<BaseResponse<String>> {
        try {
            return BaseResponse.ok(scrapeService.startScrape(strategyName, workerCount))
        } catch (e: Exception) {
            e.printStackTrace()
            throw GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    // Stop scraping for a given strategy
    @PostMapping("/stop")
    fun stopScraping(
        @RequestParam("strategyName") strategyName: String
    ): ResponseEntity<BaseResponse<String>> {
        try {
            return BaseResponse.ok(scrapeService.stopScrape(strategyName))
        } catch (e: Exception) {
            e.printStackTrace()
            throw GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    @PostMapping("/stop-all")
    fun stopAllScraping(): ResponseEntity<BaseResponse<String>> {
        return try {
            val isStopped = PlaywrightWorker.stopAllWorkers()
            if (isStopped) BaseResponse.ok("All workers have been stopped")
            else BaseResponse.internalServerError("Failed to stop workers")
        } catch (e: Exception) {
            e.printStackTrace()
            throw GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    @GetMapping("/workers")
    fun listActiveWorkers(): ResponseEntity<BaseResponse<List<String>>> {
        return BaseResponse.ok(PlaywrightWorker.activeWorkers.map { it.id })
    }

}