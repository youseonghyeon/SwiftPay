package com.swiftpay.api

import com.swiftpay.dto.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Unit>> {
        log.error("IllegalArgumentException: ${ex.message}")
        val response = ApiResponse<Unit>(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "Invalid request",
            error = ex.message
        )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(NotImplementedError::class)
    fun handleNotImplementedError(ex: NotImplementedError): ResponseEntity<ApiResponse<Unit>> {
        log.warn("NotImplementedError: ${ex.message}")
        val response = ApiResponse<Unit>(
            status = HttpStatus.NOT_IMPLEMENTED.value(),
            message = "Not implemented",
            error = ex.message
        )
        return ResponseEntity(response, HttpStatus.NOT_IMPLEMENTED)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<ApiResponse<Unit>> {
        log.error("An unexpected error occurred: ${ex.message}", ex)
        val response = ApiResponse<Unit>(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "An unexpected error occurred",
            error = ex.message
        )
        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
