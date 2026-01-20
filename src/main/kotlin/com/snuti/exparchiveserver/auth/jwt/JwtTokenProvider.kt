package com.snuti.exparchiveserver.auth.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

@Component
class JwtTokenProvider(
    @Value("\${app.jwt.secret}") secret: String,
    @Value("\${app.jwt.issuer}") private val issuer: String,
    @Value("\${app.jwt.access-token-exp-minutes}") private val expMinutes: Long
) {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    fun createAccessToken(authentication: Authentication, role: String): String {
        val now = Instant.now()
        val exp = now.plus(expMinutes, ChronoUnit.MINUTES)

        return Jwts.builder()
            .issuer(issuer)
            .subject(authentication.name) // email
            .claim("role", role)          // "USER" | "ADMIN"
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(key)
            .compact()
    }

    fun validate(token: String): Boolean = try {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
        true
    } catch (_: Exception) {
        false
    }

    fun parseEmail(token: String): String =
        Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).payload.subject

    fun parseRole(token: String): String =
        Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).payload["role"]?.toString() ?: "USER"
}