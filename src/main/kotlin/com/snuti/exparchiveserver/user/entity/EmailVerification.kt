package com.snuti.exparchiveserver.user.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import com.snuti.exparchiveserver.auth.dto.EmailVerificationPurpose

@Entity
@Table(name = "email_verifications")
class EmailVerification(

    @Column(name = "email", nullable = false)
    var email: String,

    @Column(name = "code", nullable = false)
    var code: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var purpose: EmailVerificationPurpose

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
}