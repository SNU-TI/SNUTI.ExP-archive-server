package com.snuti.exparchiveserver.user.controller

import com.snuti.exparchiveserver.lecture.dto.LectureListItemResponse
import com.snuti.exparchiveserver.lecture.dto.TagResponse
import com.snuti.exparchiveserver.lecture.service.LectureQueryService
import com.snuti.exparchiveserver.user.dto.UserInterestUpdateRequest
import com.snuti.exparchiveserver.user.service.UserFavoriteService
import com.snuti.exparchiveserver.user.service.UserInterestService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.DeleteMapping

@RestController
@RequestMapping("/users/me")
class UserInterestController(
    private val userInterestService: UserInterestService,
    private val lectureQueryService: LectureQueryService,
    private val userFavoriteService: UserFavoriteService
) {
    // 관심 태그
    @GetMapping("/interests")
    fun getInterests(
        @AuthenticationPrincipal email: String
    ): List<TagResponse> {
        return userInterestService.getInterests(email)
    }

    @PutMapping("/interests")
    fun replaceInterests(
        @AuthenticationPrincipal email: String,
        @Valid @RequestBody request: UserInterestUpdateRequest
    ): List<TagResponse> {
        return userInterestService.replaceInterests(email, request)
    }

    // 관심 강좌
    @GetMapping("/favorites")
    fun getFavorites(
        @AuthenticationPrincipal email: String,
        pageable: Pageable
    ): Page<LectureListItemResponse> {
        return lectureQueryService.getFavoriteLectures(email, pageable)
    }
    @PostMapping("/favorites/{lectureId}")
    fun addFavorite(
        @AuthenticationPrincipal email: String,
        @PathVariable lectureId: Long
    ) {
        userFavoriteService.addFavorite(email, lectureId)
    }

    @DeleteMapping("/favorites/{lectureId}")
    fun removeFavorite(
        @AuthenticationPrincipal email: String,
        @PathVariable lectureId: Long
    ) {
        userFavoriteService.removeFavorite(email, lectureId)
    }

    @GetMapping("/favorites/{lectureId}")
    fun isFavorite(
        @AuthenticationPrincipal email: String,
        @PathVariable lectureId: Long
    ): Boolean {
        return userFavoriteService.isFavorite(email, lectureId)
    }
}