package com.kanbancoders.mangatopia.scraper.strategies.asurascans

import com.kanbancoders.mangatopia.scraper.common.ScrapeStrategy
import com.kanbancoders.mangatopia.scraper.components.Chapter
import com.kanbancoders.mangatopia.scraper.components.Manga
import com.kanbancoders.mangatopia.scraper.components.MangaRepository
import com.microsoft.playwright.Browser
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.messaging.simp.SimpMessagingTemplate

class AsuraScansOneStrategy(
    private val link: String,
    private val mangaRepository: MangaRepository,
): ScrapeStrategy() {
    override suspend fun scrape(
        browser: Browser,
        simpMessagingTemplate: SimpMessagingTemplate,
    ): Flow<Manga?> = flow {
        simpMessagingTemplate.convertAndSend("/topic/progress","[MANGA : WORKING]: $link")
        val page = browser.newPage()
        try {
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
                val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
                val date = LocalDate.parse(timestamp, formatter)
                chapters.add(
                    Chapter(
                        title = mangaTitle,
                        timestamp = date.atStartOfDay(),
                        chapterUrl = it.getAttribute("href")
                    )
                )
            }
            if (chapters.isNotEmpty()) {
                chapters.reversed()
                chapters.mapIndexed { index, it ->
                    page.navigate(it.chapterUrl, domContentLoaded)
                    val imageUrls = page.querySelectorAll("#readerarea p img").map {
                        it.getAttribute("src")
                    }
                    it.imageUrls.addAll(imageUrls)
                    println(it)
                    simpMessagingTemplate.convertAndSend("/topic/progress","[CHAPTER : DONE]: ${it.title}}")
                }
                var source = mutableListOf<String>()
                if (id == null) source.add("AsuraScans")
                else {
                    source = existingManga.get().sources
                    if (source.contains("AsuraScans").not()) source.add("AsuraScans")
                }
                emit(
                    Manga(id, title, bannerUrl, synopsis, status, type, author, artist, genres, source, chapters, link)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            simpMessagingTemplate.convertAndSend("/topic/progress","[MANGA : ERROR]: ${e.message}")
        } finally {
            page.close()
        }
    }

}