package com.snuti.exparchiveserver.lecture.entity

import com.snuti.exparchiveserver.lecture.entity.LectureStatus
import com.snuti.exparchiveserver.user.entity.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "lectures")
class Lecture(
    @Column(nullable = false, length = 200)
    var title: String,

    @Column(name = "lecture_date", nullable = false)
    var lectureDate: LocalDateTime,

    @Column(length = 200)
    var location: String? = null,

    @Column(name = "lecture_summary", columnDefinition = "TEXT")
    var lectureSummary: String? = null,

    @Column(name = "lecturer_name", length = 100)
    var lecturerName: String? = null,

    @Column(length = 100)
    var topic: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: LectureStatus = LectureStatus.PUBLISHED,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    var createdBy: User
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @OneToMany(mappedBy = "lecture", cascade = [CascadeType.ALL], orphanRemoval = true)
    var articles: MutableList<Article> = mutableListOf()

    @OneToMany(mappedBy = "lecture", cascade = [CascadeType.ALL], orphanRemoval = true)
    var videos: MutableList<Video> = mutableListOf()

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}