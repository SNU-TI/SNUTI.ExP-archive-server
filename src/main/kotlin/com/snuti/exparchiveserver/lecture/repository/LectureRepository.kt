package com.snuti.exparchiveserver.lecture.repository

import com.snuti.exparchiveserver.lecture.entity.Lecture
import com.snuti.exparchiveserver.lecture.entity.LectureStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface LectureRepository : JpaRepository<Lecture, Long> {

    fun findAllByStatus(status: LectureStatus, pageable: Pageable): Page<Lecture>

    @EntityGraph(attributePaths = ["articles", "videos"])
    fun findWithDetailsByIdAndStatus(id: Long, status: LectureStatus): Optional<Lecture>

    @EntityGraph(attributePaths = ["articles", "videos"])
    fun findWithDetailsById(id: Long): Optional<Lecture>
}