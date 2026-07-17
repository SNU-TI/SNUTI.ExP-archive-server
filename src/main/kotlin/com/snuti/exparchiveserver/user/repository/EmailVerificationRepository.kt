package com.snuti.exparchiveserver.user.repository

import com.snuti.exparchiveserver.auth.dto.EmailVerificationPurpose
import com.snuti.exparchiveserver.user.entity.EmailVerification
import org.springframework.data.jpa.repository.JpaRepository

interface EmailVerificationRepository : JpaRepository<EmailVerification, Long> {
    fun findByEmailAndPurpose(
        email: String,
        purpose: EmailVerificationPurpose
    ): EmailVerification?

    fun deleteByEmailAndPurpose(
        email: String,
        purpose: EmailVerificationPurpose
    )
}