package com.snuti.exparchiveserver.common.error

import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    data class ErrorResponse(val message: String)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(e: MethodArgumentNotValidException): ErrorResponse =
        ErrorResponse(e.bindingResult.allErrors.firstOrNull()?.defaultMessage ?: "Validation error")

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadRequest(e: IllegalArgumentException): ErrorResponse =
        ErrorResponse(e.message ?: "Bad request")

    @ExceptionHandler(BadCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleAuth(e: BadCredentialsException): ErrorResponse =
        ErrorResponse(e.message ?: "Unauthorized")
}