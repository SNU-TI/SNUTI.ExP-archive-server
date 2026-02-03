package com.snuti.exparchiveserver.lecture.service

import com.snuti.exparchiveserver.lecture.dto.ArticleResponse
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
    private val lectureRepository: LectureRepository
) {
    @Transactional(readOnly = true)
    fun list(pageable: Pageable): Page<LectureListItemResponse> =
        lectureRepository.findAllByStatus(LectureStatus.PUBLISHED, pageable)
            .map { lec ->
                LectureListItemResponse(
                    id = requireNotNull(lec.id),
                    title = lec.title,
                    lectureDate = lec.lectureDate,
                    location = lec.location,
                    lecturerName = lec.lecturerName,
                    topic = lec.topic,
                    lectureSummary = lec.lectureSummary
                )
            }

    @Transactional(readOnly = true)
    fun detail(id: Long): LectureDetailResponse {
        val lec = lectureRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Lecture not found: $id") }

        // 일반 유저는 published만 보이게(추후 admin은 별도)
        if (lec.status != LectureStatus.PUBLISHED) {
            throw IllegalArgumentException("Lecture not found: $id")
        }

        return LectureDetailResponse(
            id = requireNotNull(lec.id),
            title = lec.title,
            lectureDate = lec.lectureDate,
            location = lec.location,
            lectureSummary = lec.lectureSummary,
            lecturerName = lec.lecturerName,
            topic = lec.topic,
            articles = lec.articles.map {
                ArticleResponse(
                    id = requireNotNull(it.id),
                    articleTitle = it.articleTitle,
                    author = it.author,
                    content = it.content
                )
            },
            videos = lec.videos.map {
                VideoResponse(
                    id = requireNotNull(it.id),
                    videoUrl = it.videoUrl,
                    caption = it.caption
                )
            }
        )
    }
}