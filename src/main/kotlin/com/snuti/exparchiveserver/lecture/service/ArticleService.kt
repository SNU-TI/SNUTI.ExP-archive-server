package com.snuti.exparchiveserver.lecture.service

import com.snuti.exparchiveserver.common.storage.ImageStorageService
import com.snuti.exparchiveserver.common.storage.StoredImage
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

    @Transactional
    fun createArticle(
        lectureId: Long,
        request: CreateArticleRequest,
        imageFiles: Map<String, MultipartFile>
    ): ArticleResponse {
        validateBlockRequests(request.blocks)
        require(request.blocks.none { it.existingBlockId != null }) {
            "existingBlockId cannot be used when creating an article"
        }
        validateImageFiles(
            blocks = request.blocks,
            imageFiles = imageFiles
        )
        val lecture = lectureRepository.findById(lectureId)
            .orElseThrow {
                EntityNotFoundException("Lecture not found: $lectureId")
            }
        val article = Article(
            lecture = lecture,
            articleTitle = request.articleTitle,
            author = request.author
        )
        val blocks = buildBlocks(
            article = article,
            requests = request.blocks,
            imageFiles = imageFiles
        )
        article.replaceBlocks(blocks)
        val saved = articleRepository.save(article)
        return ArticleMapper.toResponse(
            saved,
            imageStorageService
        )
    }

    @Transactional
    fun updateArticle(
        articleId: Long,
        request: UpdateArticleRequest,
        imageFiles: Map<String, MultipartFile>
    ): ArticleResponse {
        validateBlockRequests(request.blocks)
        validateImageFiles(request.blocks, imageFiles)

        val article = articleRepository.findWithLectureAndBlocksById(articleId)
            .orElseThrow {
                EntityNotFoundException("Article not found: $articleId")
            }

        val existingBlocks = article.blocks
            .mapNotNull { block ->
                block.id?.let { blockId ->
                    blockId to block
                }
            }
            .toMap()

        val oldImageKeys = article.blocks
            .filter { it.type == ArticleBlockType.IMAGE }
            .mapNotNull { it.imageKey }
            .toSet()

        val newBlocks = buildBlocks(
            article = article,
            requests = request.blocks,
            imageFiles = imageFiles,
            existingBlocks = existingBlocks
        )

        val newImageKeys = newBlocks
            .filter { it.type == ArticleBlockType.IMAGE }
            .mapNotNull { it.imageKey }
            .toSet()

        article.articleTitle = request.articleTitle
        article.author = request.author

        article.blocks.clear()
        articleRepository.flush()

        article.replaceBlocks(newBlocks)

        val savedArticle = articleRepository.saveAndFlush(article)
        val removedImageKeys = oldImageKeys - newImageKeys

        removedImageKeys.forEach { imageKey ->
            imageStorageService.deleteByKey(imageKey)
        }

        return ArticleMapper.toResponse(
            savedArticle,
            imageStorageService
        )
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
        requests: List<ArticleBlockRequest>,
        imageFiles: Map<String, MultipartFile>,
        existingBlocks: Map<Long, ArticleBlock> = emptyMap()
    ): List<ArticleBlock> {
        return requests
            .sortedBy { it.orderIndex }
            .map { request ->
                when (request.type) {
                    ArticleBlockType.TEXT -> {
                        ArticleBlock(
                            article = article,
                            type = ArticleBlockType.TEXT,
                            orderIndex = request.orderIndex,
                            textContent = request.textContent
                        ).also { it.validate() }
                    }

                    ArticleBlockType.IMAGE -> {
                        val storedImage = resolveImage(
                            request = request,
                            imageFiles = imageFiles,
                            existingBlocks = existingBlocks
                        )

                        ArticleBlock(
                            article = article,
                            type = ArticleBlockType.IMAGE,
                            orderIndex = request.orderIndex,
                            imageKey = storedImage.key,
                            originalFileName = storedImage.originalFileName
                        ).also { it.validate() }
                    }
                }
            }
    }

    private fun resolveImage(
        request: ArticleBlockRequest,
        imageFiles: Map<String, MultipartFile>,
        existingBlocks: Map<Long, ArticleBlock>
    ): StoredImage {
        request.existingBlockId?.let { blockId ->
            val existingBlock = existingBlocks[blockId]
                ?: throw IllegalArgumentException(
                    "Existing image block not found: $blockId"
                )

            require(existingBlock.type == ArticleBlockType.IMAGE) {
                "Referenced block is not an IMAGE block: $blockId"
            }

            val imageKey = existingBlock.imageKey
                ?: throw IllegalStateException(
                    "Existing IMAGE block has no imageKey: $blockId"
                )

            return StoredImage(
                key = imageKey,
                originalFileName = existingBlock.originalFileName ?: "image"
            )
        }

        request.clientImageKey?.let { clientKey ->
            val file = imageFiles[clientKey]
                ?: throw IllegalArgumentException(
                    "Image file not found: $clientKey"
                )

            return imageStorageService.store(file)
        }

        throw IllegalArgumentException(
            "IMAGE block requires existingBlockId or clientImageKey"
        )
    }

    private fun validateImageFiles(
        blocks: List<ArticleBlockRequest>,
        imageFiles: Map<String, MultipartFile>
    ) {
        val requestedFileKeys = blocks
            .filter { it.type == ArticleBlockType.IMAGE }
            .mapNotNull { it.clientImageKey }
            .filter { it.isNotBlank() }
            .toSet()

        val receivedFileKeys = imageFiles.keys

        val missingFileKeys =
            requestedFileKeys - receivedFileKeys

        require(missingFileKeys.isEmpty()) {
            "Missing image files: $missingFileKeys"
        }

        val unusedFileKeys =
            receivedFileKeys - requestedFileKeys

        require(unusedFileKeys.isEmpty()) {
            "Unused image files: $unusedFileKeys"
        }
    }

    private fun validateBlockRequests(
        blocks: List<ArticleBlockRequest>
    ) {
        require(blocks.isNotEmpty()) {
            "Article must have at least one block"
        }

        val duplicatedOrderIndexes = blocks
            .groupingBy { it.orderIndex }
            .eachCount()
            .filterValues { it > 1 }
            .keys

        require(duplicatedOrderIndexes.isEmpty()) {
            "Duplicate orderIndex values: $duplicatedOrderIndexes"
        }

        blocks.forEach { block ->
            require(block.orderIndex >= 0) {
                "orderIndex must be greater than or equal to 0"
            }

            when (block.type) {
                ArticleBlockType.TEXT -> {
                    require(!block.textContent.isNullOrBlank()) {
                        "TEXT block must have textContent"
                    }
                    require(block.clientImageKey.isNullOrBlank()) {
                        "TEXT block must not have clientImageKey"
                    }
                    require(block.existingBlockId == null) {
                        "TEXT block must not have existingBlockId"
                    }
                }

                ArticleBlockType.IMAGE -> {
                    require(block.textContent.isNullOrBlank()) {
                        "IMAGE block must not have textContent"
                    }

                    val hasNewImage =
                        !block.clientImageKey.isNullOrBlank()
                    val hasExistingImage =
                        block.existingBlockId != null

                    require(hasNewImage.xor(hasExistingImage)) {
                        "IMAGE block must have exactly one of clientImageKey or existingBlockId"
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