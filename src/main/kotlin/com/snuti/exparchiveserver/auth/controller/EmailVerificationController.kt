package com.snuti.exparchiveserver.auth.controller

import com.snuti.exparchiveserver.auth.dto.VerifyEmailCodeRequest
import com.snuti.exparchiveserver.auth.service.EmailVerificationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth/email")
class EmailVerificationController(
    private val emailVerificationService: EmailVerificationService
) {
    @PostMapping("/send")
    fun sendCode(
        @AuthenticationPrincipal email: String
    ): ResponseEntity<Void> {
        emailVerificationService.sendCode(email)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/verify")
    fun verifyCode(
        @AuthenticationPrincipal email: String,
        @Valid @RequestBody request: VerifyEmailCodeRequest
    ): ResponseEntity<Void> {
        emailVerificationService.verifyCode(email, request.code)
        return ResponseEntity.noContent().build()
    }
}