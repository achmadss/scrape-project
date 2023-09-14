package com.kanbancoders.mangatopia.scraper.common.error

import org.springframework.http.HttpStatus

data class GeneralException(
    val httpStatus: HttpStatus,
    val errorMessage: String? = null
) : Throwable(errorMessage) {
    // Additional properties and methods specific to your custom exception
}

