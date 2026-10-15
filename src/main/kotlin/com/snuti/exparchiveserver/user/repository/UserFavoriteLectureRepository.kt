package com.snuti.exparchiveserver.user.repository

import com.snuti.exparchiveserver.lecture.entity.LectureStatus
import com.snuti.exparchiveserver.user.entity.UserFavoriteLecture
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserFavoriteLectureRepository :
    JpaRepository<UserFavoriteLecture, Long> {

    fun existsByUserIdAndLectureId(
        userId: Long,
        lectureId: Long
    ): Boolean

    fun findByUserIdOrderByCreatedAtDesc(
        userId: Long
    ): List<UserFavoriteLecture>

    fun deleteByUserIdAndLectureId(
        userId: Long,
        lectureId: Long
    ): Long

    fun findByUserIdAndLectureStatusOrderByCreatedAtDesc(
        userId: Long,
        status: LectureStatus,
        pageable: Pageable
    ): Page<UserFavoriteLecture>
}