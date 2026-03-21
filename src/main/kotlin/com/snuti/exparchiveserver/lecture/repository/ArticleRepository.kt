package com.snuti.exparchiveserver.lecture.repository

import com.snuti.exparchiveserver.lecture.entity.Article
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ArticleRepository : JpaRepository<Article, Long> {

    @EntityGraph(attributePaths = ["lecture", "blocks"])
    fun findWithLectureAndBlocksById(id: Long): Optional<Article>
}