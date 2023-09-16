package com.kanbancoders.mangatopia.scraper.workers

import com.kanbancoders.mangatopia.scraper.common.ScrapeStrategy
import com.kanbancoders.mangatopia.scraper.common.error.GeneralException
import com.kanbancoders.mangatopia.scraper.components.MangaRepository
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus

class PlaywrightWorker(
    val id: String,
    private val mangaRepository: MangaRepository,
    private val strategy: ScrapeStrategy,
    private val numberOfWorkers: Int,
) : Thread() {

    companion object {
        private var shouldStop: Boolean = false
        val activeWorkers: MutableList<PlaywrightWorker> = mutableListOf()
        fun isScrapingInProgress(): Boolean {
            return activeWorkers.isNotEmpty()
        }
        fun stopAllWorkers(): Boolean {
            shouldStop = true // Signal all workers to stop
            var allStopped = true // A flag to indicate if all workers stopped
            // Wait for all workers to stop and close their resources
            synchronized(PlaywrightWorker::class.java) {
                for (worker in activeWorkers) {
                    try {
                        worker.join()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        allStopped = false // At least one worker was interrupted, so not all stopped
                    }
                }
            }
            return allStopped
        }
    }

    init {
        synchronized(PlaywrightWorker::class.java) {
            activeWorkers.add(this)
        }
    }

    override fun run() {
        // Early exit if a stop signal has been sent
        if (shouldStop || strategy.shouldStop()) {
            println("Stopping worker due to external stop signal.")
            return
        }

        try {
            val options = Playwright.CreateOptions()
            val playwright = Playwright.create(options)
            val launchOptions = BrowserType.LaunchOptions().apply {
                headless = true
                args = listOf("--mute-audio")
            }
            val browsers = List(numberOfWorkers) { playwright.chromium().launch(launchOptions) }
            runBlocking {
                val mangas = strategy.scrape(browsers)
                mangas.forEach {
                    if (it != null)  {
                        val existingMangaOptional = mangaRepository.findByTitleIgnoreCase(it.title)
                        if (existingMangaOptional.isPresent) {
                            val existingManga = existingMangaOptional.get()
                            mangaRepository.save(existingManga.copy(
                                title = it.title,
                                bannerUrl = it.bannerUrl,
                                synopsis = it.synopsis,
                                status = it.status,
                                type = it.type,
                                author = it.author,
                                artist = it.artist,
                                genres = it.genres,
                                chapters = it.chapters
                            ))
                        } else {
                            mangaRepository.save(it)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        } finally {
            // Remove this worker from the active list
            synchronized(PlaywrightWorker::class.java) {
                activeWorkers.remove(this)
            }
        }
    }

}