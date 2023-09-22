package com.kanbancoders.mangatopia.scraper.strategies.flamescans

import com.kanbancoders.mangatopia.scraper.common.ScrapeStrategy
import com.kanbancoders.mangatopia.scraper.components.Manga
import com.kanbancoders.mangatopia.scraper.components.MangaRepository
import com.microsoft.playwright.Browser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.messaging.simp.SimpMessagingTemplate

class FlameScansStrategy(
    private val baseUrl: String,
    private val mangaRepository: MangaRepository,
): ScrapeStrategy() {
    override suspend fun scrape(
        browser: Browser,
        simpMessagingTemplate: SimpMessagingTemplate
    ): Flow<Manga?> = flow {
        TODO("Not yet implemented")
    }
}