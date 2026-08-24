package com.snuti.exparchiveserver.lecture.service

import com.snuti.exparchiveserver.lecture.dto.LectureCreateRequest
import com.snuti.exparchiveserver.lecture.dto.LectureCreateResponse
import com.snuti.exparchiveserver.lecture.dto.LectureListItemResponse
import com.snuti.exparchiveserver.lecture.dto.LectureUpdateRequest
import com.snuti.exparchiveserver.lecture.dto.TagResponse
import com.snuti.exparchiveserver.lecture.entity.Lecture
import com.snuti.exparchiveserver.lecture.entity.LectureStatus
import com.snuti.exparchiveserver.lecture.entity.LectureTag
import com.snuti.exparchiveserver.lecture.entity.Tag
import com.snuti.exparchiveserver.lecture.repository.LectureRepository
import com.snuti.exparchiveserver.lecture.repository.LectureTagRepository
import com.snuti.exparchiveserver.lecture.repository.TagRepository
import com.snuti.exparchiveserver.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
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
    fun createLecture(
        request: LectureCreateRequest,
        currentUserEmail: String
    ): LectureCreateResponse {
        val user = userRepository.findByEmail(currentUserEmail)
            ?: throw IllegalArgumentException(
                "User not found: $currentUserEmail"
            )

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

        val savedLecture = lectureRepository.save(lecture)

        val tags = getOrCreateTags(request.tags)

        val lectureTags = tags.map { tag ->
            LectureTag(
                lecture = savedLecture,
                tag = tag
            )
        }

        lectureTagRepository.saveAll(lectureTags)
        savedLecture.lectureTags.addAll(lectureTags)

        return toCreateResponse(
            lecture = savedLecture,
            tags = tags
        )
    }

    @Transactional(readOnly = true)
    fun getDraftLectures(
        pageable: Pageable
    ): Page<LectureListItemResponse> {
        val sortedPageable = createSortedPageable(pageable)

        return lectureRepository
            .findAllByStatus(
                LectureStatus.DRAFT,
                sortedPageable
            )
            .map { lecture ->
                toListItemResponse(lecture)
            }
    }

    @Transactional
    fun updateLecture(
        lectureId: Long,
        request: LectureUpdateRequest
    ): LectureCreateResponse {
        val lecture = lectureRepository.findById(lectureId)
            .orElseThrow {
                IllegalArgumentException(
                    "Lecture not found: $lectureId"
                )
            }

        request.title?.let {
            lecture.title = it
        }

        request.lectureDate?.let {
            lecture.lectureDate = it
        }

        request.location?.let {
            lecture.location = it
        }

        request.lectureSummary?.let {
            lecture.lectureSummary = it
        }

        request.lecturerName?.let {
            lecture.lecturerName = it
        }

        request.topic?.let {
            lecture.topic = it
        }

        request.status?.let {
            lecture.status = it
        }

        val tags = if (request.tags != null) {
            updateLectureTags(
                lecture = lecture,
                tagNames = request.tags
            )
        } else {
            lecture.lectureTags
                .map { lectureTag ->
                    lectureTag.tag
                }
        }

        return toCreateResponse(
            lecture = lecture,
            tags = tags
        )
    }

    private fun updateLectureTags(
        lecture: Lecture,
        tagNames: List<String>
    ): List<Tag> {
        lectureTagRepository.deleteAllByLecture_Id(
            lecture.id!!
        )
        lectureTagRepository.flush()

        lecture.lectureTags.clear()

        val tags = getOrCreateTags(tagNames)

        val lectureTags = tags.map { tag ->
            LectureTag(
                lecture = lecture,
                tag = tag
            )
        }

        lectureTagRepository.saveAll(lectureTags)
        lecture.lectureTags.addAll(lectureTags)

        return tags
    }

    private fun getOrCreateTags(
        tagNames: List<String>
    ): List<Tag> {
        val normalizedTagNames = tagNames
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        return normalizedTagNames.map { name ->
            tagRepository.findByName(name)
                ?: tagRepository.save(
                    Tag(name = name)
                )
        }
    }

    private fun createSortedPageable(
        pageable: Pageable
    ): Pageable {
        return PageRequest.of(
            pageable.pageNumber,
            pageable.pageSize,
            Sort.by(
                Sort.Order.desc("id")
            )
        )
    }

    private fun toListItemResponse(
        lecture: Lecture
    ): LectureListItemResponse {
        return LectureListItemResponse(
            id = lecture.id!!,
            title = lecture.title,
            lectureDate = lecture.lectureDate,
            location = lecture.location,
            lectureSummary = lecture.lectureSummary,
            lecturerName = lecture.lecturerName,
            topic = lecture.topic,
            tags = toTagResponses(lecture)
        )
    }

    private fun toCreateResponse(
        lecture: Lecture,
        tags: List<Tag>
    ): LectureCreateResponse {
        return LectureCreateResponse(
            id = lecture.id!!,
            title = lecture.title,
            lectureDate = lecture.lectureDate,
            location = lecture.location,
            lectureSummary = lecture.lectureSummary,
            lecturerName = lecture.lecturerName,
            topic = lecture.topic,
            status = lecture.status,
            tags = tags.map { tag ->
                TagResponse(
                    id = tag.id!!,
                    name = tag.name
                )
            }
        )
    }

    private fun toTagResponses(
        lecture: Lecture
    ): List<TagResponse> {
        return lecture.lectureTags
            .sortedBy { it.createdAt }
            .map { lectureTag ->
                TagResponse(
                    id = lectureTag.tag.id!!,
                    name = lectureTag.tag.name
                )
            }
    }
}