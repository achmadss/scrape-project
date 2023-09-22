package com.kanbancoders.mangatopia.scraper.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.ui.Model

@Controller
class ProgressController {

    @GetMapping(
        path = ["progress"]
    )
    fun tryWebsocket(model: Model): String {
        return "index"
    }

}