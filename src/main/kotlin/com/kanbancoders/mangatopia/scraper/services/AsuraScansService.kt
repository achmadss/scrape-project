package com.kanbancoders.mangatopia.scraper.services

import com.kanbancoders.mangatopia.scraper.common.BaseResponse
import com.kanbancoders.mangatopia.scraper.components.Manga
import com.kanbancoders.mangatopia.scraper.components.MangaRepository
import com.kanbancoders.mangatopia.scraper.workers.PlaywrightWorker
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.WaitUntilState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class AsuraScansService @Autowired constructor(
    private val mangaRepository: MangaRepository,
    private val redisService: RedisService,
) {

    @Value("\${asurascans.base.url}")
    lateinit var baseUrl: String

    private val domContentLoaded = Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED)

    fun getAll(): ResponseEntity<BaseResponse<List<Manga>>> {
        return BaseResponse.ok(mangaRepository.findAll().toList())
    }

    @Async
    fun scrape(): String {
        // Check if scraping is already in progress
        if (PlaywrightWorker.isScrapingInProgress()) {
            return "Scraping is already in progress"
        }
        val options = Playwright.CreateOptions()
        val playwright = Playwright.create(options)
        val launchOptions = BrowserType.LaunchOptions()
        launchOptions.headless = true
        launchOptions.args = listOf("--mute-audio")

        val mainBrowser = playwright.chromium().launch(launchOptions)
        val mainPage = mainBrowser.newPage()
        val links = getLinks(mainPage)
        mainPage.close()
        mainBrowser.close()

        val chunkedLinks = links.chunked(links.size / 4)
        val workers = chunkedLinks.map {
            PlaywrightWorker(it, mangaRepository)
        }

        workers.forEach { it.start() }
        return "Scraping started"
    }

    private fun getLinks(page: Page): List<String> {
        val links = mutableListOf<String>()
        val urlTemplate = "${baseUrl}/manga/?status=&type=&order=update"
        page.navigate(urlTemplate, domContentLoaded)

        do {
            val list = page.querySelectorAll(".bsx > a")
                .map { it.getAttribute("href") }.filter { "asura.gg/discord" !in it }
            links.addAll(list)
        } while (
            page.isVisible(".r").also {
                if (it) page.navigate(
                    "${baseUrl}/manga${page.querySelector(".r").getAttribute("href")}",
                    domContentLoaded
                )
            }
        )

        return links
    }


}

