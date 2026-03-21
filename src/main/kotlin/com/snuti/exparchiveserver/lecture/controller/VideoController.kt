package com.snuti.exparchiveserver.lecture.controller

import com.snuti.exparchiveserver.lecture.dto.CreateVideoRequest
import com.snuti.exparchiveserver.lecture.dto.UpdateVideoRequest
import com.snuti.exparchiveserver.lecture.dto.VideoResponse
import com.snuti.exparchiveserver.lecture.service.VideoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class VideoController(
    private val videoService: VideoService
) {

    @PostMapping("/api/lectures/{lectureId}/videos")
    fun createVideo(
        @PathVariable lectureId: Long,
        @RequestBody request: CreateVideoRequest
    ): ResponseEntity<VideoResponse> {
        val response = videoService.createVideo(lectureId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/api/lectures/{lectureId}/videos")
    fun getVideosByLecture(
        @PathVariable lectureId: Long
    ): ResponseEntity<List<VideoResponse>> {
        val response = videoService.getVideosByLecture(lectureId)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/api/videos/{videoId}")
    fun updateVideo(
        @PathVariable videoId: Long,
        @RequestBody request: UpdateVideoRequest
    ): ResponseEntity<VideoResponse> {
        val response = videoService.updateVideo(videoId, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/api/videos/{videoId}")
    fun deleteVideo(
        @PathVariable videoId: Long
    ): ResponseEntity<Void> {
        videoService.deleteVideo(videoId)
        return ResponseEntity.noContent().build()
    }
}