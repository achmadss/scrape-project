package com.kanbancoders.mangatopia.scraper.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ProgressController {

    @GetMapping(
        path = ["progress"]
    )
    fun tryWebsocket(): String = "index"

}