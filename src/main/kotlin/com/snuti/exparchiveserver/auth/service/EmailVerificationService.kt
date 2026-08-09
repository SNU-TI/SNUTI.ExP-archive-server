package com.snuti.exparchiveserver.auth.service

import com.snuti.exparchiveserver.auth.dto.EmailVerificationPurpose
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
    @Transactional
    fun sendCode(email: String) {
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("이미 가입된 이메일입니다.")
        }

        emailVerificationRepository.deleteByEmailAndPurpose(
            email,
            EmailVerificationPurpose.SIGN_UP
        )

        val code = Random.nextInt(0, 1_000_000)
            .toString()
            .padStart(6, '0')

        val verification = EmailVerification(
            email = email,
            code = code,
            expiresAt = LocalDateTime.now().plusMinutes(5),
            purpose = EmailVerificationPurpose.SIGN_UP
        )

        emailVerificationRepository.save(verification)

        emailService.sendVerificationCode(
            to = email,
            code = code
        )
    }

    @Transactional
    fun verifyCode(
        email: String,
        code: String
    ) {
        val verification =
            emailVerificationRepository.findByEmailAndPurpose(
                email,
                EmailVerificationPurpose.SIGN_UP
            ) ?: throw IllegalArgumentException(
                "인증번호를 먼저 요청해주세요."
            )

        if (verification.expiresAt.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("인증번호가 만료되었습니다.")
        }

        if (verification.code != code) {
            throw IllegalArgumentException("인증번호가 일치하지 않습니다.")
        }

        verification.verified = true
    }
}