package com.snuti.exparchiveserver.lecture.service

import com.snuti.exparchiveserver.lecture.dto.CreateVideoRequest
import com.snuti.exparchiveserver.lecture.dto.UpdateVideoRequest
import com.snuti.exparchiveserver.lecture.dto.VideoMapper
import com.snuti.exparchiveserver.lecture.dto.VideoResponse
import com.snuti.exparchiveserver.lecture.entity.Video
import com.snuti.exparchiveserver.lecture.repository.LectureRepository
import com.snuti.exparchiveserver.lecture.repository.VideoRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class VideoService(
    private val lectureRepository: LectureRepository,
    private val videoRepository: VideoRepository
) {

    fun createVideo(lectureId: Long, request: CreateVideoRequest): VideoResponse {
        require(request.videoUrl.isNotBlank()) { "videoUrl must not be blank" }

        val lecture = lectureRepository.findById(lectureId)
            .orElseThrow { EntityNotFoundException("Lecture not found: $lectureId") }

        val video = Video(
            lecture = lecture,
            videoUrl = request.videoUrl,
            caption = request.caption
        )

        val saved = videoRepository.save(video)
        return VideoMapper.toResponse(saved)
    }

    @Transactional(readOnly = true)
    fun getVideosByLecture(lectureId: Long): List<VideoResponse> {
        if (!lectureRepository.existsById(lectureId)) {
            throw EntityNotFoundException("Lecture not found: $lectureId")
        }

        return videoRepository.findAllByLectureIdOrderByCreatedAtAsc(lectureId)
            .map { VideoMapper.toResponse(it) }
    }

    fun updateVideo(videoId: Long, request: UpdateVideoRequest): VideoResponse {
        require(request.videoUrl.isNotBlank()) { "videoUrl must not be blank" }

        val video = videoRepository.findById(videoId)
            .orElseThrow { EntityNotFoundException("Video not found: $videoId") }

        video.videoUrl = request.videoUrl
        video.caption = request.caption

        return VideoMapper.toResponse(video)
    }

    fun deleteVideo(videoId: Long) {
        val video = videoRepository.findById(videoId)
            .orElseThrow { EntityNotFoundException("Video not found: $videoId") }

        videoRepository.delete(video)
    }
}