package com.kanbancoders.mangatopia.scraper.controllers

import com.kanbancoders.mangatopia.scraper.common.BaseResponse
import com.kanbancoders.mangatopia.scraper.services.ScrapeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["scrape"])
@CrossOrigin(origins = ["*"])
class ScrapeController @Autowired constructor(
    private val scrapeService: ScrapeService,
) {

    @PostMapping("/start")
    fun startScraping(
        @RequestParam("strategyName") strategyName: String,
        @RequestParam("link") link: String? = null,
    ): ResponseEntity<BaseResponse<String>> {
        scrapeService.startScrapeUpdate(strategyName, link)
        return BaseResponse.ok("Started Scraping with strategy $strategyName")
    }

    // Stop scraping for a given strategy
    @PostMapping("/stop")
    fun stopScraping(
        @RequestParam("strategyName") strategyName: String
    ): ResponseEntity<BaseResponse<List<String>>> {
        return BaseResponse.ok(scrapeService.stopScrape(strategyName))
    }

    @PostMapping("/stop-all")
    fun stopAllScraping(): ResponseEntity<BaseResponse<List<String>>> {
        return BaseResponse.ok(scrapeService.stopAll())
    }

    @GetMapping("/workers")
    fun listActiveWorkers(): ResponseEntity<BaseResponse<List<String>>> {
        return scrapeService.listActiveWorkers()
    }

}