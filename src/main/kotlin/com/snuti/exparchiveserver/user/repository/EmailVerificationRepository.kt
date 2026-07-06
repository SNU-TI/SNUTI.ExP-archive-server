package com.snuti.exparchiveserver.user.repository

import com.snuti.exparchiveserver.user.entity.EmailVerification
import org.springframework.data.jpa.repository.JpaRepository

interface EmailVerificationRepository : JpaRepository<EmailVerification, Long> {
    fun findByEmail(email: String): EmailVerification?
    fun deleteByEmail(email: String)
}