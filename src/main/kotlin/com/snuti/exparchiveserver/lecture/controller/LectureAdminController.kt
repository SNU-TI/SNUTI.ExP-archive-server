package com.snuti.exparchiveserver.lecture.controller

import com.snuti.exparchiveserver.lecture.dto.ArticleCreateRequest
import com.snuti.exparchiveserver.lecture.dto.ArticleResponse
import com.snuti.exparchiveserver.lecture.dto.LectureCreateRequest
import com.snuti.exparchiveserver.lecture.dto.LectureCreateResponse
import com.snuti.exparchiveserver.lecture.service.ArticleService
import com.snuti.exparchiveserver.lecture.service.LectureAdminService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
class LectureAdminController(
    private val lectureAdminService: LectureAdminService,
    private val articleService: ArticleService
) {

    @Operation(summary = "강연 생성")
    @PostMapping("/lectures")
    @ResponseStatus(HttpStatus.CREATED)
    fun createLecture(
        @Valid @RequestBody request: LectureCreateRequest,
        authentication: Authentication
    ): LectureCreateResponse {
        return lectureAdminService.createLecture(request, authentication.name)
    }

    @Operation(summary = "강연에 기사 추가")
    @PostMapping("/lectures/{lectureId}/articles")
    @ResponseStatus(HttpStatus.CREATED)
    fun createArticle(
        @PathVariable lectureId: Long,
        @Valid @RequestBody request: ArticleCreateRequest
    ): ArticleResponse {
        return articleService.createArticle(lectureId, request)
    }
}