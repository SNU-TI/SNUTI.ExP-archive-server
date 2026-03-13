package com.snuti.exparchiveserver.lecture.controller

import com.snuti.exparchiveserver.lecture.dto.LectureDetailResponse
import com.snuti.exparchiveserver.lecture.dto.LectureListItemResponse
import com.snuti.exparchiveserver.lecture.service.LectureQueryService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lectures")
class LectureController(
    private val lectureQueryService: LectureQueryService
) {

    @Operation(summary = "강연 목록 조회")
    @GetMapping
    fun getLectures(
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<LectureListItemResponse> {
        return lectureQueryService.getLectures(pageable)
    }

    @Operation(summary = "강연 상세 조회")
    @GetMapping("/{id}")
    fun getLectureDetail(
        @PathVariable id: Long
    ): LectureDetailResponse {
        return lectureQueryService.getLectureDetail(id)
    }
}