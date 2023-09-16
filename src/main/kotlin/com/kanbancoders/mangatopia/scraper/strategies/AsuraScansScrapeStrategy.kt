package com.kanbancoders.mangatopia.scraper.strategies

import com.kanbancoders.mangatopia.scraper.common.ScrapeStrategy
import com.kanbancoders.mangatopia.scraper.common.error.GeneralException
import com.kanbancoders.mangatopia.scraper.components.Chapter
import com.kanbancoders.mangatopia.scraper.components.Manga
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.WaitUntilState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus

class AsuraScansScrapeStrategy: ScrapeStrategy() {

    @Value("\${asurascans.base.url}")
    lateinit var baseUrl: String

    override suspend fun scrape(browsers: List<Browser>): List<Manga?> {
        try {
            val links = getLinks(browsers.first().newPage())
            val chunkedLinks = links.chunked((links.size + browsers.size - 1) / browsers.size)

            return coroutineScope {
                val result = chunkedLinks.mapIndexed { index, chunk ->
                    async {
                        try {
                            val scrapedChunk = scrapeChunk(browsers[index].newPage(), chunk)
                            scrapedChunk
                        } finally {
                            browsers[index].close() // Close the browser after scraping the chunk
                        }
                    }
                }.awaitAll()
                result.flatten()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    private fun scrapeChunk(page: Page, links: List<String>): List<Manga?> {
        return links.map { link ->
            scrapeManga(page, link)
        }
    }

    private fun scrapeManga(page: Page, link: String): Manga? {
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