package com.kanbancoders.mangatopia.scraper.services

import jakarta.annotation.PostConstruct
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.stereotype.Service

@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    private lateinit var valueOps: ValueOperations<String, Any>

    @PostConstruct
    private fun init() {
        valueOps = redisTemplate.opsForValue()
    }

    fun setKey(key: String, value: Any) {
        valueOps.set(key, value)
    }

    fun getKey(key: String): Any? {
        return valueOps.get(key)
    }
}
