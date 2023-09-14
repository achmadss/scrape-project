package com.kanbancoders.mangatopia.scraper.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime

data class BaseResponse<T>(
    var statusCode: Int,
    var data: T,
    var message: String = "",
    var timestamp: String = LocalDateTime.now().toString()
) {
    companion object {
        fun <T> ok(data: T, message: String = HttpStatus.OK.reasonPhrase): ResponseEntity<BaseResponse<T>> {
            return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse(statusCode = HttpStatus.OK.value(), data, message)
            )
        }

        fun <T> created(data: T, message: String = HttpStatus.CREATED.reasonPhrase): ResponseEntity<BaseResponse<T>> {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse(statusCode = HttpStatus.CREATED.value(), data, message)
            )
        }

        fun <T> badRequest(data: T, message: String = HttpStatus.BAD_REQUEST.reasonPhrase): ResponseEntity<BaseResponse<T>> {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                BaseResponse(statusCode = HttpStatus.BAD_REQUEST.value(), data, message)
            )
        }

        fun <T> unauthorized(data: T, message: String = HttpStatus.UNAUTHORIZED.reasonPhrase): ResponseEntity<BaseResponse<T>> {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                BaseResponse(statusCode = HttpStatus.UNAUTHORIZED.value(), data, message)
            )
        }

        fun <T> forbidden(data: T, message: String = HttpStatus.FORBIDDEN.reasonPhrase): ResponseEntity<BaseResponse<T>> {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                BaseResponse(statusCode = HttpStatus.FORBIDDEN.value(), data, message)
            )
        }

        fun <T> notFound(data: T, message: String = HttpStatus.NOT_FOUND.reasonPhrase): ResponseEntity<BaseResponse<T>> {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                BaseResponse(statusCode = HttpStatus.NOT_FOUND.value(), data, message)
            )
        }

        fun <T> unprocessableEntity(data: T, message: String = HttpStatus.UNPROCESSABLE_ENTITY.reasonPhrase): ResponseEntity<BaseResponse<T>> {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                BaseResponse(statusCode = HttpStatus.UNPROCESSABLE_ENTITY.value(), data, message)
            )
        }

        fun <T> internalServerError(data: T, message: String = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase): ResponseEntity<BaseResponse<T>> {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                BaseResponse(statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(), data, message)
            )
        }

        fun <T> badGateway(data: T, message: String = HttpStatus.BAD_GATEWAY.reasonPhrase): ResponseEntity<BaseResponse<T>> {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                BaseResponse(statusCode = HttpStatus.BAD_GATEWAY.value(), data, message)
            )
        }

        fun <T> serviceUnavailable(data: T, message: String = HttpStatus.SERVICE_UNAVAILABLE.reasonPhrase): ResponseEntity<BaseResponse<T>> {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                BaseResponse(statusCode = HttpStatus.SERVICE_UNAVAILABLE.value(), data, message)
            )
        }
    }
}
