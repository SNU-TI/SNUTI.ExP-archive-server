package com.snuti.exparchiveserver.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:Email
    @field:NotBlank
    val email: String,

    @field:Size(min = 8)
    val password: String
)

data class LoginRequest(
    @field:Email
    @field:NotBlank
    val email: String,

    @field:NotBlank
    val password: String
)

data class ChangePasswordRequest(
    @field:NotBlank
    val currentPassword: String,

    @field:NotBlank
    @field:Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    val newPassword: String
)

data class VerifyEmailCodeRequest(
    @field:NotBlank
    val code: String
)

data class AuthResponse(
    val accessToken: String
)