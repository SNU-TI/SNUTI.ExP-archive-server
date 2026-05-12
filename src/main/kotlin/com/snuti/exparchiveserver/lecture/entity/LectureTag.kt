package com.snuti.exparchiveserver.lecture.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "lecture_tags",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_lecture_tag",
            columnNames = ["lecture_id", "tag_id"]
        )
    ]
)
class LectureTag(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    var lecture: Lecture,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    var tag: Tag
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}