package com.snuti.exparchiveserver.lecture.controller


import com.snuti.exparchiveserver.lecture.dto.ArticleResponse
import com.snuti.exparchiveserver.lecture.dto.CreateArticleRequest
import com.snuti.exparchiveserver.lecture.dto.CreateVideoRequest
import com.snuti.exparchiveserver.lecture.dto.LectureCreateRequest
import com.snuti.exparchiveserver.lecture.dto.LectureCreateResponse
import com.snuti.exparchiveserver.lecture.dto.VideoResponse
import com.snuti.exparchiveserver.lecture.service.ArticleService
import com.snuti.exparchiveserver.lecture.service.LectureAdminService
import com.snuti.exparchiveserver.lecture.service.VideoService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import tools.jackson.databind.ObjectMapper

@RestController
@RequestMapping("/admin")
class LectureAdminController(
    private val lectureAdminService: LectureAdminService,
    private val articleService: ArticleService,
    private val videoService: VideoService,
    private val objectMapper: ObjectMapper
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

    @Operation(summary = "강연에 아티클 추가")
    @PostMapping(
        "/lectures/{lectureId}/articles",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun createArticle(
        @PathVariable lectureId: Long,
        @RequestPart("request") requestJson: String,
        multipartRequest: MultipartHttpServletRequest
    ): ArticleResponse {
        val request = objectMapper.readValue(requestJson, CreateArticleRequest::class.java)
        val fileMap = extractMultipartFiles(multipartRequest)

        return articleService.createArticle(lectureId, request, fileMap)
    }

    @Operation(summary = "강연에 비디오 추가")
    @PostMapping("/lectures/{lectureId}/videos")
    @ResponseStatus(HttpStatus.CREATED)
    fun createVideo(
        @PathVariable lectureId: Long,
        @Valid @RequestBody request: CreateVideoRequest
    ): VideoResponse {
        return videoService.createVideo(lectureId, request)
    }

    private fun extractMultipartFiles(
        multipartRequest: MultipartHttpServletRequest
    ): Map<String, MultipartFile> {
        return multipartRequest.fileMap
            .filterKeys { it != "request" }
    }
}