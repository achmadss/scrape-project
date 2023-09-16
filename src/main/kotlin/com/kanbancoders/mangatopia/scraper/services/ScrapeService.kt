package com.kanbancoders.mangatopia.scraper.services

import com.kanbancoders.mangatopia.scraper.common.BaseResponse
import com.kanbancoders.mangatopia.scraper.common.ScrapeStrategy
import com.kanbancoders.mangatopia.scraper.components.Manga
import com.kanbancoders.mangatopia.scraper.components.MangaRepository
import com.kanbancoders.mangatopia.scraper.strategies.AsuraScansScrapeStrategy
import com.kanbancoders.mangatopia.scraper.workers.PlaywrightWorker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class ScrapeService @Autowired constructor(
    private val mangaRepository: MangaRepository,
) {

    // Map to store active workers and their corresponding strategies
    private val activeStrategies: MutableMap<String, ScrapeStrategy> = mutableMapOf()

    @Async
    fun startScrape(strategyName: String, workerCount: Int): String {
        // Retrieve strategy based on the name
        val strategy = when (strategyName) {
            "AsuraScans" -> AsuraScansScrapeStrategy()
//            "FlameScans" -> FlameScansScrapeStrategy()
            else -> return "Invalid strategy name"
        }

        // Store strategy in map
        activeStrategies[strategyName] = strategy

        // Start worker with given strategy
        PlaywrightWorker(strategyName, mangaRepository, strategy, workerCount).start()

        return "Scraping started with strategy $strategyName"
    }

    fun stopScrape(strategyName: String): String {
        val strategy = activeStrategies[strategyName] ?: return "No such active strategy"

        // Stop scraping
        strategy.stopScraping()

        // Remove from active strategies
        activeStrategies.remove(strategyName)

        return "Scraping stopped for strategy $strategyName"
    }



}