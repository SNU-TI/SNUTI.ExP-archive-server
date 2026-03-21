package com.snuti.exparchiveserver.lecture.service

import com.snuti.exparchiveserver.common.storage.ImageStorageService
import com.snuti.exparchiveserver.lecture.dto.ArticleMapper
import com.snuti.exparchiveserver.lecture.dto.LectureDetailResponse
import com.snuti.exparchiveserver.lecture.dto.LectureListItemResponse
import com.snuti.exparchiveserver.lecture.dto.VideoResponse
import com.snuti.exparchiveserver.lecture.entity.LectureStatus
import com.snuti.exparchiveserver.lecture.repository.LectureRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LectureQueryService(
    private val lectureRepository: LectureRepository,
    private val imageStorageService: ImageStorageService
) {

    @Transactional(readOnly = true)
    fun getLectures(pageable: Pageable): Page<LectureListItemResponse> {
        return lectureRepository.findAllByStatus(LectureStatus.PUBLISHED, pageable)
            .map { lecture ->
                LectureListItemResponse(
                    id = lecture.id!!,
                    title = lecture.title,
                    lectureDate = lecture.lectureDate,
                    location = lecture.location,
                    lectureSummary = lecture.lectureSummary,
                    lecturerName = lecture.lecturerName,
                    topic = lecture.topic
                )
            }
    }

    @Transactional(readOnly = true)
    fun getLectureDetail(id: Long): LectureDetailResponse {
        val lecture = lectureRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Lecture not found: $id") }

        if (lecture.status != LectureStatus.PUBLISHED) {
            throw IllegalArgumentException("Lecture not found: $id")
        }

        return LectureDetailResponse(
            id = lecture.id!!,
            title = lecture.title,
            lectureDate = lecture.lectureDate,
            location = lecture.location,
            lectureSummary = lecture.lectureSummary,
            lecturerName = lecture.lecturerName,
            topic = lecture.topic,
            status = lecture.status,
            articles = lecture.articles
                .sortedBy { it.createdAt }
                .map { article -> ArticleMapper.toResponse(article, imageStorageService) },
            videos = lecture.videos
                .sortedBy { it.createdAt }
                .map { video ->
                    VideoResponse(
                        id = video.id!!,
                        lectureId = video.lecture.id!!,
                        videoUrl = video.videoUrl,
                        caption = video.caption,
                        createdAt = video.createdAt
                    )
                }
        )
    }
}