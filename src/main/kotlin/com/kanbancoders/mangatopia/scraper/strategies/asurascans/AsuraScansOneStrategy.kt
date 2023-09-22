package com.kanbancoders.mangatopia.scraper.strategies.asurascans

import com.kanbancoders.mangatopia.scraper.common.ScrapeStrategy
import com.kanbancoders.mangatopia.scraper.components.Chapter
import com.kanbancoders.mangatopia.scraper.components.Manga
import com.kanbancoders.mangatopia.scraper.components.MangaRepository
import com.microsoft.playwright.Browser
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AsuraScansOneStrategy(
    private val link: String,
    private val mangaRepository: MangaRepository,
): ScrapeStrategy() {
    override suspend fun scrape(browser: Browser): Flow<Manga?> = flow {
        val page = browser.newPage()
        page.navigate(link, domContentLoaded)
        val elements = page.querySelectorAll(".thumbook img, .thumbook .imptdt i, .thumbook .imptdt a, .entry-title")
        val bannerUrl = elements[0].getAttribute("src")
        val status = elements[1].innerText()
        val type = elements[2].innerText()
        val title = elements[3].innerText()
        val existingManga = mangaRepository.findByTitleIgnoreCase(title)
        var id: String? = null
        if (existingManga.isPresent) id = existingManga.get().id
        val existingChaptersTitles = if (existingManga.isPresent) {
            existingManga.get().chapters.map { it.title }.toSet()
        } else emptySet()
        val synopsis =  page.querySelectorAll(".entry-content p").joinToString("") { it.innerText() }
        val genres = page.querySelectorAll(".mgen a").map { it.innerText() }.toMutableList()
        val metadata = page.querySelectorAll(".infox .fmed")
        val author = metadata[1].querySelector("span").innerText()
        val artist = metadata[2].querySelector("span").innerText()
        val chapterLinks = page.querySelectorAll("#chapterlist a")
        val chapters = mutableListOf<Chapter>()
        chapterLinks.forEach {
            val mangaTitle = it.querySelector(".chapternum").innerText()
            if (existingChaptersTitles.contains(mangaTitle)) return@forEach
            val timestamp = it.querySelector(".chapterdate").innerText()
            chapters.add(
                Chapter(
                    title = mangaTitle,
                    timestamp = LocalDateTime.parse(timestamp),
                    chapterUrl = it.getAttribute("href")
                )
            )
        }
        if (chapters.isNotEmpty()) {
            chapters.reversed()
            chapters.map {
                page.navigate(it.chapterUrl, domContentLoaded)
                val imageUrls = page.querySelectorAll("#readerarea p img").map {
                    it.getAttribute("src")
                }
                it.imageUrls.addAll(imageUrls)
            }
            emit(
                Manga(id, title, bannerUrl, synopsis, status, type, author, artist, genres, chapters, link)
            )
        }
    }

}