package com.kanbancoders.mangatopia.scraper.strategies.asurascans

import com.kanbancoders.mangatopia.scraper.common.ScrapeStrategy
import com.kanbancoders.mangatopia.scraper.common.sortDescending
import com.kanbancoders.mangatopia.scraper.components.Chapter
import com.kanbancoders.mangatopia.scraper.components.Manga
import com.kanbancoders.mangatopia.scraper.components.MangaRepository
import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.messaging.simp.SimpMessagingTemplate

class AsuraScansStrategy(
    private val baseUrl: String,
    private val mangaRepository: MangaRepository,
): ScrapeStrategy() {

    override suspend fun scrape(
        browser: Browser,
        simpMessagingTemplate: SimpMessagingTemplate,
    ): Flow<Manga?> = flow {
        simpMessagingTemplate.convertAndSend("/topic/progress", "STATUS:ONGOING")
        val page = browser.newPage()
        val lastUpdated = mangaRepository.findTopByOrderByUpdatedAtDesc()
            .map { it.title }
            .orElse("")
        val links = accumulateLinksUntilLastUpdated(page, 1, lastUpdated).reversed()
        page.close()
        for (link in links) {
            simpMessagingTemplate.convertAndSend("/topic/progress","[MANGA : WORKING]: $link")
            val manga = scrapeManga(browser, link, simpMessagingTemplate)
            emit(manga)
        }
    }

    private fun accumulateLinksUntilLastUpdated(
        page: Page,
        pageNumber: Int,
        lastUpdated: String,
        accumulatedLinks: MutableList<String> = mutableListOf()
    ): List<String> {
        navigateToPage(page, pageNumber)

        val currentData = fetchTitlesAndLinks(page)
        val currentLinks = currentData.map { it.second }
        accumulatedLinks += currentLinks

        val lastUpdatedIndex = currentData.indexOfFirst { it.first == lastUpdated }
        if (lastUpdatedIndex != -1) return accumulatedLinks.subList(
            0, accumulatedLinks.size - currentLinks.size + lastUpdatedIndex + 1
        )

        return if (shouldNavigateToNextPage(page)) {
            accumulateLinksUntilLastUpdated(page, pageNumber + 1, lastUpdated, accumulatedLinks)
        } else accumulatedLinks
    }

    private fun navigateToPage(page: Page, pageNumber: Int) {
        page.navigate("${baseUrl}/manga/?page=${pageNumber}&order=update", domContentLoaded)
    }

    private fun fetchTitlesAndLinks(page: Page): List<Pair<String, String>> {
        return page.querySelectorAll(".bsx > a").map {
            it.getAttribute("title") to it.getAttribute("href")
        }
    }

    private fun shouldNavigateToNextPage(page: Page): Boolean {
        val nextPageExist = page.querySelector(".r")
        return nextPageExist != null && fetchTitlesAndLinks(page).map { it.second }.isNotEmpty()
    }

    private fun scrapeManga(
        browser: Browser,
        link: String,
        simpMessagingTemplate: SimpMessagingTemplate,
    ): Manga? {
        val context = browser.newContext()
        context.clearCookies()
        val page = context.newPage()
        return try {
            page.navigate(link, domContentLoaded)
            val elements = page.querySelectorAll(".thumbook img, .thumbook .imptdt i, .thumbook .imptdt a, .entry-title")
            val bannerUrl = elements[0].getAttribute("src")
            val status = elements[1].innerText()
            val type = elements[2].innerText()
            val title = elements[3].innerText()
            val existingManga = mangaRepository.findByTitleIgnoreCase(title)
            var id: String? = null
            var chapters = mutableListOf<Chapter>()
            val existingChaptersTitles = if (existingManga.isPresent) {
                println("chapters exist: ${existingManga.get().chapters.map { it.title }}")
                existingManga.get().chapters.map { it.title }.toSet()
            } else emptySet()
            if (existingManga.isPresent) id = existingManga.get().id
            val synopsis =  page.querySelectorAll(".entry-content p").joinToString("") { it.innerText() }
            val genres = page.querySelectorAll(".mgen a").map { it.innerText() }.toMutableList()
            val metadata = page.querySelectorAll(".infox .fmed")
            val author = metadata[1].querySelector("span").innerText()
            val artist = metadata[2].querySelector("span").innerText()
            val chapterLinks = page.querySelectorAll("#chapterlist a")
            chapterLinks.forEach {
                val mangaTitle = it.querySelector(".chapternum").innerText()
                if (existingChaptersTitles.contains(mangaTitle)) {
                    println("$mangaTitle already exist")
                    return@forEach
                }
                val timestamp = it.querySelector(".chapterdate").innerText()
                val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
                val date = LocalDate.parse(timestamp, formatter)
                chapters.add(Chapter(
                    title = mangaTitle,
                    timestamp = date.atStartOfDay(),
                    chapterUrl = it.getAttribute("href")
                ))
            }
            chapters.map {
                page.navigate(it.chapterUrl, domContentLoaded)
                val imageUrls = page.querySelectorAll("#readerarea p img").map {
                    it.getAttribute("src")
                }
                it.imageUrls.addAll(imageUrls)
                println(it)
                simpMessagingTemplate.convertAndSend("/topic/progress","[CHAPTER : DONE]: ${it.title}")
            }
            if (existingManga.isPresent) chapters.addAll(existingManga.get().chapters)
            chapters = chapters.sortDescending()
            var source = mutableListOf<String>()
            if (id == null) source.add("AsuraScans")
            else {
                source = existingManga.get().sources
                if (source.contains("AsuraScans").not()) source.add("AsuraScans")
            }
            page.close()
            Manga(id, title, bannerUrl, synopsis, status, type, author, artist, genres, source, chapters, link)
        } catch (e: Exception) {
            e.printStackTrace()
            simpMessagingTemplate.convertAndSend("/topic/progress","[MANGA : ERROR]: ${e.message}")
            null
        } finally {
            page.close()
        }
    }

}