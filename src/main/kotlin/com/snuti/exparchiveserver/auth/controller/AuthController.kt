package com.snuti.exparchiveserver.auth.controller

import com.snuti.exparchiveserver.auth.dto.AuthResponse
import com.snuti.exparchiveserver.auth.dto.LoginRequest
import com.snuti.exparchiveserver.auth.dto.RegisterRequest
import com.snuti.exparchiveserver.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
}