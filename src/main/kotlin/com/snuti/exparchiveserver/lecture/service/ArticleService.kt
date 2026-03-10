package com.snuti.exparchiveserver.lecture.service

import com.snuti.exparchiveserver.lecture.dto.ArticleCreateRequest
import com.snuti.exparchiveserver.lecture.dto.ArticleResponse
import com.snuti.exparchiveserver.lecture.entity.Article
import com.snuti.exparchiveserver.lecture.repository.ArticleRepository
import com.snuti.exparchiveserver.lecture.repository.LectureRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArticleService(
    private val articleRepository: ArticleRepository,
    private val lectureRepository: LectureRepository
) {

    @Transactional
    fun createArticle(lectureId: Long, request: ArticleCreateRequest): ArticleResponse {
        val lecture = lectureRepository.findById(lectureId)
            .orElseThrow { IllegalArgumentException("Lecture not found: $lectureId") }

        val article = Article(
            lecture = lecture,
            articleTitle = request.articleTitle,
            author = request.author,
            content = request.content
        )

        val saved = articleRepository.save(article)

        return ArticleResponse(
            id = saved.id!!,
            lectureId = lecture.id!!,
            articleTitle = saved.articleTitle,
            author = saved.author,
            content = saved.content
        )
    }

    @Transactional(readOnly = true)
    fun getArticle(id: Long): ArticleResponse {
        val article = articleRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Article not found: $id") }

        return ArticleResponse(
            id = article.id!!,
            lectureId = article.lecture.id!!,
            articleTitle = article.articleTitle,
            author = article.author,
            content = article.content
        )
    }
}