package com.snuti.exparchiveserver.lecture.dto

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
    val articleTitle: String,
    val author: String?,
    val content: String
)

data class VideoResponse(
    val id: Long,
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
    val articles: List<ArticleResponse>,
    val videos: List<VideoResponse>
)