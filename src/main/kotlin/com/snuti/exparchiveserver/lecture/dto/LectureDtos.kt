package com.snuti.exparchiveserver.lecture.dto

import com.snuti.exparchiveserver.lecture.entity.Article
import com.snuti.exparchiveserver.lecture.entity.ArticleBlockType
import com.snuti.exparchiveserver.lecture.entity.LectureStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
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
    val blocks: List<ArticleBlockResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ArticleBlockResponse(
    val id: Long,
    val type: ArticleBlockType,
    val orderIndex: Int,
    val textContent: String?,
    val imageUrl: String?,
    val originalFileName: String?
)

data class VideoResponse(
    val id: Long,
    val lectureId: Long,
    val videoUrl: String,
    val caption: String?,
    val createdAt: LocalDateTime
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

data class CreateArticleRequest(
    @field:NotBlank
    val articleTitle: String,

    val author: String? = null,

    @field:NotEmpty
    @field:Valid
    val blocks: List<ArticleBlockRequest>
)

data class UpdateArticleRequest(
    @field:NotBlank
    val articleTitle: String,

    val author: String? = null,

    @field:NotEmpty
    @field:Valid
    val blocks: List<ArticleBlockRequest>
)

data class ArticleBlockRequest(
    @field:NotNull
    val type: ArticleBlockType,

    @field:NotNull
    val orderIndex: Int,

    val textContent: String? = null,

    /**
     * IMAGE block일 때 프론트가 임시 키를 넣는다.
     * 예: "img-1", "img-2"
     * multipart 파일 part 이름도 동일하게 맞춘다.
     */
    val clientImageKey: String? = null
)

data class CreateVideoRequest(
    val videoUrl: String,
    val caption: String? = null
)

data class UpdateVideoRequest(
    val videoUrl: String,
    val caption: String? = null
)