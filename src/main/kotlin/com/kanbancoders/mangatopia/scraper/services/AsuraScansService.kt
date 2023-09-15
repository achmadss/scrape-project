package com.kanbancoders.mangatopia.scraper.services

import com.kanbancoders.mangatopia.scraper.common.BaseResponse
import com.kanbancoders.mangatopia.scraper.components.Chapter
import com.kanbancoders.mangatopia.scraper.components.Manga
import com.kanbancoders.mangatopia.scraper.components.MangaRepository
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
) {

    @Value("\${asurascans.base.url}")
    lateinit var baseUrl: String

    private val domContentLoaded = Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED)

    fun getAll(): ResponseEntity<BaseResponse<List<Manga>>> {
        return BaseResponse.ok(mangaRepository.findAll().toList())
    }

    @Async
    fun scrape() {
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
            PlaywrightWorker(it) // Pass WebSocket handler
        }

        workers.forEach { it.start() }
        workers.forEach { it.join() }
    }

    private fun getLinks(page: Page): List<String> {
        val links = mutableListOf<String>()
        val urlTemplate = "${baseUrl}/manga/?status=&type=&order=update"
        page.navigate(urlTemplate, domContentLoaded)

        do {
            val list = page.querySelectorAll(".bsx > a").map { it.getAttribute("href") }.filter { "asura.gg/discord" !in it }
            links.addAll(list)
        } while (
            page.isVisible(".r").also {
                if (it) page.navigate("${baseUrl}/manga${page.querySelector(".r").getAttribute("href")}", domContentLoaded)
            }
        )

        return links
    }

    private fun fetchMangaDetails(page: Page, link: String): Manga? {
        return try {
            page.navigate(link, domContentLoaded.setTimeout(0.0))
            val elements = page.querySelectorAll(".thumbook img, .thumbook .imptdt i, .thumbook .imptdt a, .entry-title")
            val banner = elements[0].getAttribute("src")
            val status = elements[1].innerText()
            val type = elements[2].innerText()
            val title = elements[3].innerText()
            val synopsis =  page.querySelectorAll(".entry-content p").joinToString("") { it.innerText() }
            val genres = page.querySelectorAll(".mgen a").map { it.innerText() }
            val metadata = page.querySelectorAll(".infox .fmed")
            val author = metadata[1].querySelector("span").innerText()
            val artist = metadata[2].querySelector("span").innerText()
            val chapterLinks = page.querySelectorAll("#chapterlist a").map { it.getAttribute("href") }
            val images = chapterLinks.map {
                page.navigate(it, domContentLoaded)
                page.querySelectorAll("#readerarea p img").map { it.getAttribute("src") }
            }
            val chapters = images.map { Chapter(imageUrls = it.toMutableList()) }
            Manga(
                title = title,
                bannerUrl = banner,
                synopsis = synopsis,
                status = status,
                type = type,
                author = author,
                artist = artist,
                genres = genres.toMutableList(),
                chapters = chapters.toMutableList(),
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    inner class PlaywrightWorker(
        private val links: List<String>,
    ) : Thread() {
        override fun run() {
            val options = Playwright.CreateOptions()
            val playwright = Playwright.create(options)
            val launchOptions = BrowserType.LaunchOptions()
            launchOptions.headless = true
            launchOptions.args = listOf("--mute-audio")

            val browser = playwright.chromium().launch(launchOptions)
            val page = browser.newPage()

            for ((index, link) in links.withIndex()) {
                val manga = fetchMangaDetails(page, link)
                println("${index+1}. ${manga?.title}")
                if (manga != null) mangaRepository.save(manga)
            }

            page.close()
            browser.close()
            playwright.close()
        }
    }
}

