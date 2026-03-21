package com.snuti.exparchiveserver.lecture.dto

import com.snuti.exparchiveserver.lecture.entity.Video

object VideoMapper {
    fun toResponse(video: Video): VideoResponse {
        return VideoResponse(
            id = video.id!!,
            lectureId = video.lecture.id!!,
            videoUrl = video.videoUrl,
            caption = video.caption,
            createdAt = video.createdAt
        )
    }
}