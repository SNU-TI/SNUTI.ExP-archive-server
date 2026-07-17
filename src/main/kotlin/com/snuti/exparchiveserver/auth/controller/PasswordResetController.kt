package com.snuti.exparchiveserver.auth.controller

import com.snuti.exparchiveserver.auth.dto.PasswordResetRequest
import com.snuti.exparchiveserver.auth.dto.PasswordResetSendRequest
import com.snuti.exparchiveserver.auth.dto.PasswordResetVerifyRequest
import com.snuti.exparchiveserver.auth.service.PasswordResetService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth/password/reset")
class PasswordResetController(
    private val passwordResetService: PasswordResetService
) {

    @PostMapping("/send")
    fun sendCode(
        @Valid @RequestBody request: PasswordResetSendRequest
    ): ResponseEntity<Void> {
        passwordResetService.sendCode(request.email)

        return ResponseEntity.noContent().build()
    }

    @PostMapping("/verify")
    fun verifyCode(
        @Valid @RequestBody request: PasswordResetVerifyRequest
    ): ResponseEntity<Void> {
        passwordResetService.verifyCode(
            email = request.email,
            code = request.code
        )

        return ResponseEntity.noContent().build()
    }

    @PostMapping
    fun resetPassword(
        @Valid @RequestBody request: PasswordResetRequest
    ): ResponseEntity<Void> {
        passwordResetService.resetPassword(
            email = request.email,
            code = request.code,
            newPassword = request.newPassword
        )

        return ResponseEntity.noContent().build()
    }
}