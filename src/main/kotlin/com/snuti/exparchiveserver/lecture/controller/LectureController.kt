package com.snuti.exparchiveserver.lecture.controller

import com.snuti.exparchiveserver.lecture.dto.LectureDetailResponse
import com.snuti.exparchiveserver.lecture.dto.LectureListItemResponse
import com.snuti.exparchiveserver.lecture.service.LectureQueryService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import org.springframework.security.core.annotation.AuthenticationPrincipal

@RestController
@RequestMapping("/lectures")
class LectureController(
    private val lectureQueryService: LectureQueryService
) {

    @Operation(summary = "강연 목록 조회")
    @GetMapping
    fun getLectures(
        @RequestParam(required = false) tagId: Long?,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<LectureListItemResponse> {
        return if (tagId == null) {
            lectureQueryService.getLectures(pageable)
        } else {
            lectureQueryService.getLecturesByTag(tagId, pageable)
        }
    }

    @Operation(summary = "관심 키워드 기반 강좌 추천")
    @GetMapping("/recommended")
    fun getRecommendedLectures(
        @AuthenticationPrincipal email: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<LectureListItemResponse> {
        return lectureQueryService.getRecommendedLectures(
            email = email,
            pageable = pageable
        )
    }


    @GetMapping("/search")
    fun searchLectures(
        @RequestParam keyword: String,
        pageable: Pageable
    ): Page<LectureListItemResponse> {
        return lectureQueryService.searchLectures(keyword, pageable)
    }

    @Operation(summary = "강연 상세 조회")
    @GetMapping("/{id}")
    fun getLectureDetail(
        @PathVariable id: Long
    ): LectureDetailResponse {
        return lectureQueryService.getLectureDetail(id)
    }
}