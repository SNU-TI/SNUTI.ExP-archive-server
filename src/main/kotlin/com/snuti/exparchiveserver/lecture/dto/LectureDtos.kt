package com.snuti.exparchiveserver.lecture.dto

import com.snuti.exparchiveserver.lecture.entity.LectureStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class LectureListItemResponse(
    val id: Long,
    val title: String,
    val lectureDate: LocalDateTime,
    val location: String?,
    val lecturerName: String?,
    val topic: String?,
    val lectureSummary: String?
)

data class ArticleResponse(
    val id: Long,
    val lectureId: Long,
    val articleTitle: String,
    val author: String?,
    val content: String
)

data class VideoResponse(
    val id: Long,
    val lectureId: Long,
    val videoUrl: String,
    val caption: String?
)

data class LectureDetailResponse(
    val id: Long,
    val title: String,
    val lectureDate: LocalDateTime,
    val location: String?,
    val lectureSummary: String?,
    val lecturerName: String?,
    val topic: String?,
    val status: LectureStatus,
    val articles: List<ArticleResponse>,
    val videos: List<VideoResponse>
)

data class LectureCreateRequest(
    @field:NotBlank
    val title: String,

    @field:NotNull
    val lectureDate: LocalDateTime,

    val location: String? = null,
    val lectureSummary: String? = null,
    val lecturerName: String? = null,
    val topic: String? = null,
    val status: LectureStatus = LectureStatus.PUBLISHED
)

data class LectureCreateResponse(
    val id: Long,
    val title: String,
    val lectureDate: LocalDateTime,
    val location: String?,
    val lectureSummary: String?,
    val lecturerName: String?,
    val topic: String?,
    val status: LectureStatus
)

data class ArticleCreateRequest(
    @field:NotBlank
    val articleTitle: String,

    val author: String? = null,

    @field:NotBlank
    val content: String
)