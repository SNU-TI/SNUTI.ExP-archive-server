package com.snuti.exparchiveserver.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
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

data class SendEmailCodeRequest(
    @field:NotBlank
    @field:Email
    val email: String
)

data class VerifyEmailCodeRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Pattern(
        regexp = "\\d{6}",
        message = "인증번호는 6자리 숫자여야 합니다."
    )
    val code: String
)

data class AuthResponse(
    val accessToken: String
)

data class PasswordResetSendRequest(

    @field:NotBlank(message = "이메일을 입력해주세요.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String
)

data class PasswordResetVerifyRequest(

    @field:NotBlank(message = "이메일을 입력해주세요.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,

    @field:NotBlank(message = "인증번호를 입력해주세요.")
    @field:Pattern(
        regexp = "\\d{6}",
        message = "인증번호는 6자리 숫자여야 합니다."
    )
    val code: String
)

data class PasswordResetRequest(

    @field:NotBlank(message = "이메일을 입력해주세요.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,

    @field:NotBlank(message = "인증번호를 입력해주세요.")
    @field:Pattern(
        regexp = "\\d{6}",
        message = "인증번호는 6자리 숫자여야 합니다."
    )
    val code: String,

    @field:NotBlank(message = "새 비밀번호를 입력해주세요.")
    @field:Size(
        min = 8,
        message = "비밀번호는 8자 이상이어야 합니다."
    )
    val newPassword: String
)