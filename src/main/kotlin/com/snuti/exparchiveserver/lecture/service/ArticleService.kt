package com.snuti.exparchiveserver.lecture.service

import com.snuti.exparchiveserver.common.storage.ImageStorageService
import com.snuti.exparchiveserver.lecture.dto.ArticleBlockRequest
import com.snuti.exparchiveserver.lecture.dto.ArticleMapper
import com.snuti.exparchiveserver.lecture.dto.ArticleResponse
import com.snuti.exparchiveserver.lecture.dto.CreateArticleRequest
import com.snuti.exparchiveserver.lecture.dto.UpdateArticleRequest
import com.snuti.exparchiveserver.lecture.entity.Article
import com.snuti.exparchiveserver.lecture.entity.ArticleBlock
import com.snuti.exparchiveserver.lecture.entity.ArticleBlockType
import com.snuti.exparchiveserver.lecture.repository.ArticleRepository
import com.snuti.exparchiveserver.lecture.repository.LectureRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class ArticleService(
    private val articleRepository: ArticleRepository,
    private val lectureRepository: LectureRepository,
    private val imageStorageService: ImageStorageService
) {

    fun createArticle(
        lectureId: Long,
        request: CreateArticleRequest,
        imageFiles: Map<String, MultipartFile>
    ): ArticleResponse {
        validateBlockRequests(request.blocks)

        val lecture = lectureRepository.findById(lectureId)
            .orElseThrow { EntityNotFoundException("Lecture not found: $lectureId") }

        val article = Article(
            lecture = lecture,
            articleTitle = request.articleTitle,
            author = request.author
        )

        val blocks = buildBlocks(article, request.blocks, imageFiles)
        article.replaceBlocks(blocks)

        val saved = articleRepository.save(article)
        return ArticleMapper.toResponse(saved, imageStorageService)
    }

    fun updateArticle(
        articleId: Long,
        request: UpdateArticleRequest,
        imageFiles: Map<String, MultipartFile>
    ): ArticleResponse {
        validateBlockRequests(request.blocks)

        val article = articleRepository.findWithLectureAndBlocksById(articleId)
            .orElseThrow { EntityNotFoundException("Article not found: $articleId") }

        deleteExistingImages(article)

        article.articleTitle = request.articleTitle
        article.author = request.author

        // 1. 기존 블록 제거 후 먼저 flush
        article.blocks.clear()
        articleRepository.saveAndFlush(article)

        // 2. 새 블록 생성 후 다시 저장
        val newBlocks = buildBlocks(article, request.blocks, imageFiles)
        article.replaceBlocks(newBlocks)

        val saved = articleRepository.saveAndFlush(article)
        return ArticleMapper.toResponse(saved, imageStorageService)
    }

    @Transactional(readOnly = true)
    fun getArticle(articleId: Long): ArticleResponse {
        val article = articleRepository.findWithLectureAndBlocksById(articleId)
            .orElseThrow { EntityNotFoundException("Article not found: $articleId") }

        return ArticleMapper.toResponse(article, imageStorageService)
    }

    fun deleteArticle(articleId: Long) {
        val article = articleRepository.findWithLectureAndBlocksById(articleId)
            .orElseThrow { EntityNotFoundException("Article not found: $articleId") }

        deleteExistingImages(article)
        articleRepository.delete(article)
    }

    private fun buildBlocks(
        article: Article,
        blockRequests: List<ArticleBlockRequest>,
        imageFiles: Map<String, MultipartFile>
    ): List<ArticleBlock> {
        return blockRequests
            .sortedBy { it.orderIndex }
            .mapIndexed { index, blockRequest ->
                when (blockRequest.type) {
                    ArticleBlockType.TEXT -> {
                        val block = ArticleBlock(
                            article = article,
                            type = ArticleBlockType.TEXT,
                            orderIndex = index,
                            textContent = blockRequest.textContent?.trim()
                        )
                        block.validate()
                        block
                    }

                    ArticleBlockType.IMAGE -> {
                        val imageKey = blockRequest.clientImageKey
                            ?: throw IllegalArgumentException("IMAGE block requires clientImageKey")

                        val multipartFile = imageFiles[imageKey]
                            ?: throw IllegalArgumentException("No image file found for key: $imageKey")

                        val storedImage = imageStorageService.store(multipartFile)

                        val block = ArticleBlock(
                            article = article,
                            type = ArticleBlockType.IMAGE,
                            orderIndex = index,
                            imageKey = storedImage.key,
                            originalFileName = storedImage.originalFileName
                        )
                        block.validate()
                        block
                    }
                }
            }
    }

    private fun validateBlockRequests(blocks: List<ArticleBlockRequest>) {
        require(blocks.isNotEmpty()) { "Article must contain at least one block" }

        val orderIndexes = blocks.map { it.orderIndex }
        require(orderIndexes.size == orderIndexes.toSet().size) {
            "orderIndex values must be unique"
        }

        blocks.forEach { block ->
            when (block.type) {
                ArticleBlockType.TEXT -> {
                    require(!block.textContent.isNullOrBlank()) {
                        "TEXT block must have textContent"
                    }
                }

                ArticleBlockType.IMAGE -> {
                    require(!block.clientImageKey.isNullOrBlank()) {
                        "IMAGE block must have clientImageKey"
                    }
                }
            }
        }
    }

    private fun deleteExistingImages(article: Article) {
        article.blocks
            .filter { it.type == ArticleBlockType.IMAGE && !it.imageKey.isNullOrBlank() }
            .forEach { imageStorageService.deleteByKey(it.imageKey!!) }
    }
}