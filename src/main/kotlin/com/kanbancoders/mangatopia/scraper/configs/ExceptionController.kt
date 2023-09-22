package com.kanbancoders.mangatopia.scraper.configs

import com.kanbancoders.mangatopia.scraper.common.BaseResponse
import com.kanbancoders.mangatopia.scraper.common.error.GeneralException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ExceptionController: ResponseEntityExceptionHandler() {

    val errorLogger: Logger = LoggerFactory.getLogger(ExceptionController::class.java)

    @ExceptionHandler(GeneralException::class)
    fun handleAPIException(
        e: GeneralException,
        request: HttpServletRequest,
    ): ResponseEntity<BaseResponse<Nothing?>> {
        errorLogger.error("\nException occurred at ${request.method} ${request.requestURI}: \n${e.message}\n")
        val errorMessage = e.message ?: e.httpStatus.reasonPhrase
        val errorResponse = BaseResponse(
            statusCode = e.httpStatus.value(),
            data = null,
            message = errorMessage,
        )
        return ResponseEntity.status(e.httpStatus).body(errorResponse)
    }

//    @ExceptionHandler(Exception::class)
//    fun handleException(
//        e: Exception,
//        request: HttpServletRequest,
//    ): ResponseEntity<BaseResponse<Nothing?>> {
//        errorLogger.error("\nException occurred at ${request.method} ${request.requestURI}: \n${e.message}\n")
//        val statusCode = HttpStatus.INTERNAL_SERVER_ERROR
//        val errorMessage = e.message ?: statusCode.reasonPhrase
//        val errorResponse = BaseResponse(
//            statusCode = statusCode.value(),
//            data = null,
//            message = errorMessage,
//        )
//        return ResponseEntity.status(statusCode).body(errorResponse)
//    }

}