package com.snuti.exparchiveserver.user.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "email_verifications")
class EmailVerification(
    @Column(name = "email", nullable = false, unique = true)
    var email: String,

    @Column(name = "code", nullable = false)
    var code: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
}