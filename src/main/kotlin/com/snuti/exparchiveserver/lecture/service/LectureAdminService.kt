package com.snuti.exparchiveserver.lecture.service

import com.snuti.exparchiveserver.lecture.dto.LectureCreateRequest
import com.snuti.exparchiveserver.lecture.dto.LectureCreateResponse
import com.snuti.exparchiveserver.lecture.dto.TagResponse
import com.snuti.exparchiveserver.lecture.entity.Lecture
import com.snuti.exparchiveserver.lecture.entity.LectureTag
import com.snuti.exparchiveserver.lecture.entity.Tag
import com.snuti.exparchiveserver.lecture.repository.LectureRepository
import com.snuti.exparchiveserver.lecture.repository.TagRepository
import com.snuti.exparchiveserver.lecture.repository.LectureTagRepository
import com.snuti.exparchiveserver.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LectureAdminService(
    private val lectureRepository: LectureRepository,
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val lectureTagRepository: LectureTagRepository
) {

    @Transactional
    fun createLecture(request: LectureCreateRequest, currentUserEmail: String): LectureCreateResponse {
        val user = userRepository.findByEmail(currentUserEmail)
            ?: throw IllegalArgumentException("User not found: $currentUserEmail")

        val lecture = Lecture(
            title = request.title,
            lectureDate = request.lectureDate,
            location = request.location,
            lectureSummary = request.lectureSummary,
            lecturerName = request.lecturerName,
            topic = request.topic,
            status = request.status,
            createdBy = user
        )

        val saved = lectureRepository.save(lecture)

        val tagNames = request.tags
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val tags = tagNames.map { name ->
            tagRepository.findByName(name)
                ?: tagRepository.save(Tag(name = name))
        }

        lectureTagRepository.saveAll(
            tags.map { tag ->
                LectureTag(
                    lecture = saved,
                    tag = tag
                )
            }
        )

        return LectureCreateResponse(
            id = saved.id!!,
            title = saved.title,
            lectureDate = saved.lectureDate,
            location = saved.location,
            lectureSummary = saved.lectureSummary,
            lecturerName = saved.lecturerName,
            topic = saved.topic,
            status = saved.status,
            tags = tags.map {
                TagResponse(
                    id = it.id!!,
                    name = it.name
                )
            }
        )
    }
}