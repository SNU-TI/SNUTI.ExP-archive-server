package com.snuti.exparchiveserver.auth.service

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender
) {
    fun sendVerificationCode(to: String, code: String) {
        val message = SimpleMailMessage().apply {
            setTo(to)
            subject = "ExpArchive 이메일 인증번호"
            text = """
                ExpArchive 이메일 인증번호입니다.

                인증번호: $code

                이 인증번호는 5분 동안 유효합니다.
            """.trimIndent()
        }

        mailSender.send(message)
    }
}