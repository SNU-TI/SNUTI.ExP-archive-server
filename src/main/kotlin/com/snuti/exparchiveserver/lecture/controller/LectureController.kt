package com.snuti.exparchiveserver.lecture.controller

import com.snuti.exparchiveserver.lecture.dto.LectureDetailResponse
import com.snuti.exparchiveserver.lecture.dto.LectureListItemResponse
import com.snuti.exparchiveserver.lecture.service.LectureQueryService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/lectures")
class LectureController(
    private val lectureQueryService: LectureQueryService
) {
    @GetMapping
    fun list(@PageableDefault(size = 20) pageable: Pageable): Page<LectureListItemResponse> =
        lectureQueryService.list(pageable)

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Long): LectureDetailResponse =
        lectureQueryService.detail(id)
}