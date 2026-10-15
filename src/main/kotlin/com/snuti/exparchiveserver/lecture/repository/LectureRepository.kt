package com.snuti.exparchiveserver.lecture.repository

import com.snuti.exparchiveserver.lecture.entity.Lecture
import com.snuti.exparchiveserver.lecture.entity.LectureStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface LectureRepository : JpaRepository<Lecture, Long> {

    fun findAllByStatus(status: LectureStatus, pageable: Pageable): Page<Lecture>

    fun findByStatusAndTitleContaining(
        status: LectureStatus,
        keyword: String,
        pageable: Pageable
    ): Page<Lecture>

    @Query(
        value = """
            SELECT DISTINCT l
            FROM Lecture l
            JOIN l.lectureTags lt
            WHERE l.status = :status
              AND lt.tag.id = :tagId
        """,
        countQuery = """
            SELECT COUNT(DISTINCT l)
            FROM Lecture l
            JOIN l.lectureTags lt
            WHERE l.status = :status
              AND lt.tag.id = :tagId
        """
    )
    fun findByStatusAndTagId(
        @Param("status") status: LectureStatus,
        @Param("tagId") tagId: Long,
        pageable: Pageable
    ): Page<Lecture>

    @Query(
        value = """
            SELECT DISTINCT l
            FROM Lecture l
            JOIN l.lectureTags lt
            WHERE l.status = :status
              AND lt.tag.id IN :tagIds
        """,
        countQuery = """
            SELECT COUNT(DISTINCT l)
            FROM Lecture l
            JOIN l.lectureTags lt
            WHERE l.status = :status
              AND lt.tag.id IN :tagIds
        """
    )
    fun findPublishedByTagIds(
        @Param("status") status: LectureStatus,
        @Param("tagIds") tagIds: List<Long>,
        pageable: Pageable
    ): Page<Lecture>
}