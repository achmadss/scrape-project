package com.kanbancoders.mangatopia.scraper.common

import com.kanbancoders.mangatopia.scraper.components.Manga
import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitUntilState

abstract class ScrapeStrategy {

    private var shouldStop: Boolean = false
    val domContentLoaded: Page.NavigateOptions = Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED)

    open fun stopScraping() {
        shouldStop = true
    }

    open fun shouldStop(): Boolean {
        return shouldStop
    }

    // Specific strategy classes should override this method
    abstract suspend fun scrape(browsers: List<Browser>): List<Manga?>

}
