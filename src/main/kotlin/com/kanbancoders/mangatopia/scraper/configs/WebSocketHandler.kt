package com.kanbancoders.mangatopia.scraper.configs

import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class WebSocketHandler : TextWebSocketHandler() {
    private val sessions = mutableListOf<WebSocketSession>()

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        // Handle incoming messages if needed
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.add(session)
    }

    fun sendProgress(progress: String) {
        for (session in sessions) {
            session.sendMessage(TextMessage(progress))
        }
    }
}
