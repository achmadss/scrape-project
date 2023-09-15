package com.kanbancoders.mangatopia.scraper.workers

import com.kanbancoders.mangatopia.scraper.components.Chapter
import com.kanbancoders.mangatopia.scraper.components.Manga
import com.kanbancoders.mangatopia.scraper.components.MangaRepository
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.WaitUntilState

class PlaywrightWorker(
    private val links: List<String>,
    private val mangaRepository: MangaRepository,
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

    private val domContentLoaded = Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED)

    private fun fetchMangaDetails(page: Page, link: String): Manga? {
        return try {
            page.navigate(link, domContentLoaded.setTimeout(0.0))
            val elements = page.querySelectorAll(".thumbook img, .thumbook .imptdt i, .thumbook .imptdt a, .entry-title")
            val bannerUrl = elements[0].getAttribute("src")
            val status = elements[1].innerText()
            val type = elements[2].innerText()
            val title = elements[3].innerText()
            val synopsis =  page.querySelectorAll(".entry-content p").joinToString("") { it.innerText() }
            val genres = page.querySelectorAll(".mgen a").map { it.innerText() }.toMutableList()
            val metadata = page.querySelectorAll(".infox .fmed")
            val author = metadata[1].querySelector("span").innerText()
            val artist = metadata[2].querySelector("span").innerText()
            val chapters = page.querySelectorAll("#chapterlist a").map {
                page.navigate(it.getAttribute("href"), domContentLoaded)
                Chapter(
                    title = it.querySelector(".chapternum").innerText(),
                    timestamp = it.querySelector(".chapterdate").innerText(),
                    imageUrls = page.querySelectorAll("#readerarea p img").map {
                        it.getAttribute("src")
                    }.toMutableList()
                )
            }.toMutableList()
            Manga(null, title, bannerUrl, synopsis, status, type, author, artist, genres, chapters)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun run() {
        // Early exit if a stop signal has been sent
        if (shouldStop) {
            println("Stopping worker due to external stop signal.")
            return
        }

        val options = Playwright.CreateOptions()
        val playwright = Playwright.create(options)
        val launchOptions = BrowserType.LaunchOptions().apply {
            headless = true
            args = listOf("--mute-audio")
        }

        val browser = playwright.chromium().launch(launchOptions)
        val page = browser.newPage()

        try {
            for ((index, link) in links.withIndex()) {
                if (shouldStop) {
                    println("Stopping worker due to external stop signal.")
                    break
                }

                val manga = fetchMangaDetails(page, link) ?: continue
                println("${index + 1}. ${manga.title}")

                // Upsert logic
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
                } else {
                    mangaRepository.save(manga)
                }
            }
        } finally {
            // Cleanup
            page.close()
            browser.close()
            playwright.close()

            // Remove this worker from the active list
            synchronized(PlaywrightWorker::class.java) {
                activeWorkers.remove(this)
            }
        }
    }

}