package com.snuti.exparchiveserver.lecture.dto

import com.snuti.exparchiveserver.common.storage.ImageStorageService
import com.snuti.exparchiveserver.lecture.entity.Article

object ArticleMapper {
    fun toResponse(article: Article, imageStorageService: ImageStorageService): ArticleResponse {
        return ArticleResponse(
            id = article.id!!,
            lectureId = article.lecture.id!!,
            articleTitle = article.articleTitle,
            author = article.author,
            blocks = article.blocks
                .sortedBy { it.orderIndex }
                .map { block ->
                    ArticleBlockResponse(
                        id = block.id!!,
                        type = block.type,
                        orderIndex = block.orderIndex,
                        textContent = block.textContent,
                        imageUrl = block.imageKey?.let { imageStorageService.getUrl(it) },
                        originalFileName = block.originalFileName
                    )
                },
            createdAt = article.createdAt,
            updatedAt = article.updatedAt
        )
    }
}