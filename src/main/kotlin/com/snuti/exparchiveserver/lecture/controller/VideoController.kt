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
    @PutMapping("/admin/videos/{videoId}")
    fun updateVideo(
        @PathVariable videoId: Long,
        @RequestBody request: UpdateVideoRequest
    ): ResponseEntity<VideoResponse> {
        val response = videoService.updateVideo(videoId, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/admin/videos/{videoId}")
    fun deleteVideo(
        @PathVariable videoId: Long
    ): ResponseEntity<Void> {
        videoService.deleteVideo(videoId)
        return ResponseEntity.noContent().build()
    }
}