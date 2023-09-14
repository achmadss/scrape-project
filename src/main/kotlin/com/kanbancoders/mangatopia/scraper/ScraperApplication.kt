package com.kanbancoders.mangatopia.scraper

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class ScraperApplication

fun main(args: Array<String>) {
	runApplication<ScraperApplication>(*args)
}
