package com.kanbancoders.mangatopia.scraper.services

import com.kanbancoders.mangatopia.scraper.common.BaseResponse
import com.kanbancoders.mangatopia.scraper.common.error.GeneralException
import com.kanbancoders.mangatopia.scraper.components.MangaRepository
import com.kanbancoders.mangatopia.scraper.strategies.asurascans.AsuraScansOneStrategy
import com.kanbancoders.mangatopia.scraper.strategies.asurascans.AsuraScansStrategy
import com.kanbancoders.mangatopia.scraper.workers.PlaywrightWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class ScrapeService @Autowired constructor(
    private val mangaRepository: MangaRepository,
): DisposableBean {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    val activeWorkers: MutableMap<String, PlaywrightWorker> = mutableMapOf()

    @Value("\${asurascans.base.url}")
    lateinit var asurascansBaseUrl: String

    override fun destroy() {
        coroutineScope.cancel()  // Cancel all child coroutines when the service is destroyed
    }

    @Async
    fun startScrapeUpdate(
        strategyName: String,
        link: String?,
    ) {
        if (activeWorkers.containsKey(strategyName)) {
            println("Strategy $strategyName is already active.")
            return
        }
        // Retrieve strategy based on the name
        val strategy = when (strategyName) {
            "AsuraScans" -> AsuraScansStrategy(asurascansBaseUrl, mangaRepository)
            "AsuraScansOne" -> {
                if (link == null) throw GeneralException(HttpStatus.BAD_REQUEST, "link must not be null")
                AsuraScansOneStrategy(link, mangaRepository)
            }
//            "FlameScans" -> FlameScansScrapeStrategy()
            else -> return
        }
        val worker = PlaywrightWorker(strategy, strategyName, mangaRepository) { activeWorkers.remove(it) }
        activeWorkers[strategyName] = worker
        worker.start(coroutineScope)
    }

    fun stopScrape(strategyName: String): List<String> {
        val worker = activeWorkers[strategyName]
            ?: throw GeneralException(HttpStatus.BAD_REQUEST, "No such active strategy")
        worker.stop()
        activeWorkers.remove(strategyName)
        return listOf(strategyName)
    }

    fun stopAll(): List<String> {
        val strategies = activeWorkers.map { it.key }
        activeWorkers.forEach { (_, worker) -> worker.stop() }
        activeWorkers.clear()
        return strategies
    }

    fun listActiveWorkers() = BaseResponse.ok(activeWorkers.map { it.key })

}