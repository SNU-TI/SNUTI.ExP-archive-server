package com.snuti.exparchiveserver.auth.service

import com.snuti.exparchiveserver.user.entity.EmailVerification
import com.snuti.exparchiveserver.user.repository.EmailVerificationRepository
import com.snuti.exparchiveserver.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.random.Random

@Service
@Transactional
class EmailVerificationService(
    private val userRepository: UserRepository,
    private val emailVerificationRepository: EmailVerificationRepository,
    private val emailService: EmailService
) {
    fun sendCode(email: String) {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")

        if (user.emailVerified) {
            throw IllegalArgumentException("이미 인증된 이메일입니다.")
        }

        val code = Random.nextInt(100000, 1000000).toString()
        val expiresAt = LocalDateTime.now().plusMinutes(5)

        val existing = emailVerificationRepository.findByEmail(email)

        if (existing == null) {
            emailVerificationRepository.save(
                EmailVerification(
                    email = email,
                    code = code,
                    expiresAt = expiresAt
                )
            )
        } else {
            existing.code = code
            existing.expiresAt = expiresAt
        }

        emailService.sendVerificationCode(email, code)
    }

    fun verifyCode(email: String, code: String) {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")

        val verification = emailVerificationRepository.findByEmail(email)
            ?: throw IllegalArgumentException("인증번호를 먼저 요청해주세요.")

        if (verification.expiresAt.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("인증번호가 만료되었습니다.")
        }

        if (verification.code != code) {
            throw IllegalArgumentException("인증번호가 일치하지 않습니다.")
        }

        user.emailVerified = true
        emailVerificationRepository.delete(verification)
    }
}