package com.snuti.exparchiveserver.lecture.repository

import com.snuti.exparchiveserver.lecture.entity.Lecture
import com.snuti.exparchiveserver.lecture.entity.LectureStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface LectureRepository : JpaRepository<Lecture, Long> {

    fun findAllByStatus(status: LectureStatus, pageable: Pageable): Page<Lecture>
}