package com.kanbancoders.mangatopia.scraper.common

import com.kanbancoders.mangatopia.scraper.components.Manga
import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitUntilState
import kotlinx.coroutines.flow.Flow

abstract class ScrapeStrategy {
    val domContentLoaded: Page.NavigateOptions =
        Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(60000.0)
    abstract suspend fun scrape(browser: Browser): Flow<Manga?>
}
