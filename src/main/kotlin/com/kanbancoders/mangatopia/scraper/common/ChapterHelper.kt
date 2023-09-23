package com.kanbancoders.mangatopia.scraper.common

import com.kanbancoders.mangatopia.scraper.components.Chapter

fun String.extractVolumeAndChapter(): Pair<Int, Int> {
    val volumeRegex = "Vol\\.\\s*(\\d+)".toRegex()
    val chapterRegex = "Ch\\.\\s*(\\d+)|Chapter\\s*(\\d+)".toRegex()

    val volumeMatch = volumeRegex.find(this)
    val chapterMatch = chapterRegex.find(this)

    val volume = volumeMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
    val chapter = chapterMatch?.groupValues?.drop(1)?.firstOrNull { it.isNotEmpty() }?.toIntOrNull() ?: 0

    return Pair(volume, chapter)
}

fun MutableList<Chapter>.sortDescending(): MutableList<Chapter> {
    return this.map { it.title.extractVolumeAndChapter() to it }.sortedWith(
        compareByDescending<Pair<Pair<Int, Int>, Chapter>> { it.first.first }.thenByDescending { it.first.second }
    ).map { it.second }.toMutableList()
}