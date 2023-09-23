package com.kanbancoders.mangatopia.scraper.workers

import com.kanbancoders.mangatopia.scraper.common.ScrapeStrategy
import com.kanbancoders.mangatopia.scraper.components.Manga
import com.kanbancoders.mangatopia.scraper.components.MangaRepository
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.TimeoutError
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.springframework.messaging.simp.SimpMessagingTemplate

class PlaywrightWorker(
    private val strategy: ScrapeStrategy,
    private val strategyName: String,
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val mangaRepository: MangaRepository,
    private val onJobComplete: (String) -> Unit,
) {

    private var job: Job? = null
    private val options = Playwright.CreateOptions()
    private val playwright: Playwright = Playwright.create(options)
    private val launchOptions = BrowserType.LaunchOptions().apply {
        headless = true
        args = listOf("--mute-audio")
    }
    private val browser: Browser = playwright.chromium().launch(launchOptions)

    fun start(scope: CoroutineScope) {
        job = scope.launch {
            try { strategy.scrape(browser, simpMessagingTemplate).collect {
                if (it != null) {
                    saveMangaToDatabase(it.copy(updatedAt = LocalDateTime.now()))
                    simpMessagingTemplate.convertAndSend(
                        "/topic/progress", "[MANGA : DONE]: ${it.title}"
                    )
                }
            }}
            catch (e: Exception) {
                e.printStackTrace()
                simpMessagingTemplate.convertAndSend("/topic/progress", "STATUS:ERROR")
            }
            finally { stop() }
        }
    }

    fun stop() {
        try {
            browser.close()
            playwright.close()
            simpMessagingTemplate.convertAndSend("/topic/progress", "STATUS:DONE")
        } catch (e: Exception) { e.printStackTrace() } finally {
            job?.cancel()
            onJobComplete(strategyName)
        }
    }

    private fun saveMangaToDatabase(manga: Manga) {
        val existingMangaOptional = mangaRepository.findByTitleIgnoreCase(manga.title)
        if (existingMangaOptional.isPresent) {
            val existingManga = existingMangaOptional.get()
            mangaRepository.save(existingManga.copy(
                title = manga.title,
                bannerUrl = manga.bannerUrl,
                synopsis = manga.synopsis,
                status = manga.status,
                type = manga.type,
                author = manga.author,
                artist = manga.artist,
                genres = manga.genres,
                chapters = manga.chapters
            ))
        } else mangaRepository.save(manga)
    }

}