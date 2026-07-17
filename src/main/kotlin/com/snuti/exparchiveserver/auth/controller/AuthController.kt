package com.snuti.exparchiveserver.auth.controller

import com.snuti.exparchiveserver.auth.dto.AuthResponse
import com.snuti.exparchiveserver.auth.dto.ChangePasswordRequest
import com.snuti.exparchiveserver.auth.dto.LoginRequest
import com.snuti.exparchiveserver.auth.dto.RegisterRequest
import com.snuti.exparchiveserver.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody req: RegisterRequest): AuthResponse =
        authService.register(req)

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): AuthResponse =
        authService.login(req)

    @PatchMapping("/password")
    fun changePassword(
        @AuthenticationPrincipal email: String,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<Void> {
        authService.changePassword(email, request)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/me")
    fun deleteAccount(
        @AuthenticationPrincipal email: String
    ): ResponseEntity<Void> {
        authService.deleteAccount(email)
        return ResponseEntity.noContent().build()
    }
}