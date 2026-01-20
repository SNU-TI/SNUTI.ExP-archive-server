package com.snuti.exparchiveserver.auth.service

import com.snuti.exparchiveserver.auth.dto.AuthResponse
import com.snuti.exparchiveserver.auth.dto.LoginRequest
import com.snuti.exparchiveserver.auth.dto.RegisterRequest
import com.snuti.exparchiveserver.auth.jwt.JwtTokenProvider
import com.snuti.exparchiveserver.user.entity.Role
import com.snuti.exparchiveserver.user.entity.User
import com.snuti.exparchiveserver.user.repository.UserRepository
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {
    @Transactional
    fun register(req: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(req.email)) {
            throw IllegalArgumentException("Email already exists")
        }

        val user = userRepository.save(
            User(
                email = req.email,
                passwordHash = passwordEncoder.encode(req.password)!!,
                role = Role.USER
            )
        )

        val auth: Authentication =
            UsernamePasswordAuthenticationToken(user.email, null, emptyList())

        val token = jwtTokenProvider.createAccessToken(auth, user.role.name)
        return AuthResponse(token)
    }

    fun login(req: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(req.email)
            ?: throw BadCredentialsException("Invalid credentials")

        if (!passwordEncoder.matches(req.password, user.passwordHash)) {
            throw BadCredentialsException("Invalid credentials")
        }

        val auth: Authentication =
            UsernamePasswordAuthenticationToken(user.email, null, emptyList())

        val token = jwtTokenProvider.createAccessToken(auth, user.role.name)
        return AuthResponse(token)
    }
}