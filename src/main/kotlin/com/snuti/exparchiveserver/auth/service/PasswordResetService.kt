package com.snuti.exparchiveserver.auth.service

import com.snuti.exparchiveserver.auth.dto.EmailVerificationPurpose
import com.snuti.exparchiveserver.user.entity.EmailVerification
import com.snuti.exparchiveserver.user.repository.EmailVerificationRepository
import com.snuti.exparchiveserver.user.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val emailVerificationRepository: EmailVerificationRepository,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun sendCode(email: String) {
        userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("가입되지 않은 이메일입니다.")

        emailVerificationRepository.deleteByEmailAndPurpose(
            email,
            EmailVerificationPurpose.PASSWORD_RESET
        )

        val code = Random.nextInt(0, 1_000_000)
            .toString()
            .padStart(6, '0')

        val verification = EmailVerification(
            email = email,
            code = code,
            expiresAt = LocalDateTime.now().plusMinutes(5),
            purpose = EmailVerificationPurpose.PASSWORD_RESET
        )

        emailVerificationRepository.save(verification)

        emailService.sendVerificationCode(
            to = email,
            code = code
        )
    }

    @Transactional(readOnly = true)
    fun verifyCode(
        email: String,
        code: String
    ) {
        validateCode(email, code)
    }

    @Transactional
    fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ) {
        validateCode(email, code)

        val user = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("가입되지 않은 이메일입니다.")

        user.passwordHash = passwordEncoder.encode(newPassword).toString()

        emailVerificationRepository.deleteByEmailAndPurpose(
            email,
            EmailVerificationPurpose.PASSWORD_RESET
        )
    }

    private fun validateCode(
        email: String,
        code: String
    ): EmailVerification {
        val verification =
            emailVerificationRepository.findByEmailAndPurpose(
                email,
                EmailVerificationPurpose.PASSWORD_RESET
            ) ?: throw IllegalArgumentException(
                "비밀번호 재설정 인증번호를 먼저 요청해주세요."
            )

        if (verification.expiresAt.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("인증번호가 만료되었습니다.")
        }

        if (verification.code != code) {
            throw IllegalArgumentException("인증번호가 일치하지 않습니다.")
        }

        return verification
    }
}