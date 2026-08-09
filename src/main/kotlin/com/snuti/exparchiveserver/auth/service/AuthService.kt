package com.snuti.exparchiveserver.auth.service

import com.snuti.exparchiveserver.auth.dto.AuthResponse
import com.snuti.exparchiveserver.auth.dto.ChangePasswordRequest
import com.snuti.exparchiveserver.auth.dto.EmailVerificationPurpose
import com.snuti.exparchiveserver.auth.dto.LoginRequest
import com.snuti.exparchiveserver.auth.dto.RegisterRequest
import com.snuti.exparchiveserver.auth.jwt.JwtTokenProvider
import com.snuti.exparchiveserver.user.entity.Role
import com.snuti.exparchiveserver.user.entity.User
import com.snuti.exparchiveserver.user.repository.EmailVerificationRepository
import com.snuti.exparchiveserver.user.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val emailVerificationRepository: EmailVerificationRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Transactional
    fun register(req: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(req.email)) {
            throw IllegalArgumentException("Email already exists")
        }

        val verification =
            emailVerificationRepository.findByEmailAndPurpose(
                req.email,
                EmailVerificationPurpose.SIGN_UP
            ) ?: throw IllegalArgumentException(
                "이메일 인증이 필요합니다."
            )

        if (!verification.verified) {
            throw IllegalArgumentException(
                "이메일 인증이 완료되지 않았습니다."
            )
        }

        val user = userRepository.save(
            User(
                email = req.email,
                passwordHash = passwordEncoder.encode(req.password)!!,
                role = Role.USER
            ).apply {
                emailVerified = true
            }
        )

        /*
         * 회원가입에 사용한 인증 정보는 더 이상 필요하지 않으므로 삭제
         */
        emailVerificationRepository.delete(verification)

        val auth: Authentication =
            UsernamePasswordAuthenticationToken(
                user.email,
                null,
                emptyList()
            )

        val token =
            jwtTokenProvider.createAccessToken(
                auth,
                user.role.name
            )

        return AuthResponse(token)
    }

    fun login(req: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(req.email)
            ?: throw BadCredentialsException("Invalid credentials")

        if (!passwordEncoder.matches(req.password, user.passwordHash)) {
            throw BadCredentialsException("Invalid credentials")
        }

        val auth: Authentication =
            UsernamePasswordAuthenticationToken(
                user.email,
                null,
                emptyList()
            )

        val token =
            jwtTokenProvider.createAccessToken(
                auth,
                user.role.name
            )

        return AuthResponse(token)
    }

    @Transactional
    fun changePassword(
        email: String,
        request: ChangePasswordRequest
    ) {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException(
                "사용자를 찾을 수 없습니다."
            )

        if (!passwordEncoder.matches(
                request.currentPassword,
                user.passwordHash
            )
        ) {
            throw IllegalArgumentException(
                "현재 비밀번호가 일치하지 않습니다."
            )
        }

        if (passwordEncoder.matches(
                request.newPassword,
                user.passwordHash
            )
        ) {
            throw IllegalArgumentException(
                "새 비밀번호는 현재 비밀번호와 달라야 합니다."
            )
        }

        user.passwordHash =
            passwordEncoder.encode(request.newPassword)!!
    }

    @Transactional
    fun deleteAccount(email: String) {
        val user = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException(
                "사용자를 찾을 수 없습니다."
            )

        userRepository.delete(user)
    }
}