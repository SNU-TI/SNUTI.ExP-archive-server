package com.snuti.exparchiveserver.user.entity

import com.snuti.exparchiveserver.lecture.entity.Lecture
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_favorite_lectures",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_favorite_lecture",
            columnNames = ["user_id", "lecture_id"]
        )
    ]
)
class UserFavoriteLecture(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    val lecture: Lecture,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
)