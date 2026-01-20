package com.snuti.exparchiveserver.auth.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        val token = header?.takeIf { it.startsWith("Bearer ") }?.substring(7)

        if (!token.isNullOrBlank() && jwtTokenProvider.validate(token)) {
            val email = jwtTokenProvider.parseEmail(token)
            val role = jwtTokenProvider.parseRole(token)

            val auth = UsernamePasswordAuthenticationToken(
                email,
                null,
                listOf(SimpleGrantedAuthority("ROLE_$role"))
            )
            SecurityContextHolder.getContext().authentication = auth
        }

        filterChain.doFilter(request, response)
    }
}