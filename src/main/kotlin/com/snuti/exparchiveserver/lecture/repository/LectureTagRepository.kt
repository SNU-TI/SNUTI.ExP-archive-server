package com.snuti.exparchiveserver.lecture.repository

import com.snuti.exparchiveserver.lecture.entity.LectureTag
import org.springframework.data.jpa.repository.JpaRepository

interface LectureTagRepository : JpaRepository<LectureTag, Long> {
    fun findAllByLectureId(lectureId: Long): List<LectureTag>
}