package com.snuti.exparchiveserver.lecture.controller


import com.snuti.exparchiveserver.lecture.dto.ArticleResponse
import com.snuti.exparchiveserver.lecture.dto.UpdateArticleRequest
import com.snuti.exparchiveserver.lecture.service.ArticleService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import tools.jackson.databind.ObjectMapper

@RestController
class ArticleController(
    private val articleService: ArticleService,
    private val objectMapper: ObjectMapper
) {

    @Operation(summary = "아티클 단건 조회")
    @GetMapping("/articles/{articleId}")
    fun getArticle(
        @PathVariable articleId: Long
    ): ResponseEntity<ArticleResponse> {
        return ResponseEntity.ok(articleService.getArticle(articleId))
    }

    @Operation(summary = "아티클 수정")
    @PutMapping(
        "/admin/articles/{articleId}",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun updateArticle(
        @PathVariable articleId: Long,
        @RequestPart("request") requestJson: String,
        multipartRequest: MultipartHttpServletRequest
    ): ResponseEntity<ArticleResponse> {
        val request = objectMapper.readValue(requestJson, UpdateArticleRequest::class.java)
        val fileMap = extractMultipartFiles(multipartRequest)

        return ResponseEntity.ok(articleService.updateArticle(articleId, request, fileMap))
    }

    @Operation(summary = "아티클 삭제")
    @DeleteMapping("/admin/articles/{articleId}")
    fun deleteArticle(
        @PathVariable articleId: Long
    ): ResponseEntity<Void> {
        articleService.deleteArticle(articleId)
        return ResponseEntity.noContent().build()
    }

    private fun extractMultipartFiles(
        multipartRequest: MultipartHttpServletRequest
    ): Map<String, MultipartFile> {
        return multipartRequest.fileMap.filterKeys { it != "request" }
    }
}