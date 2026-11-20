package com.snuti.exparchiveserver.lecture.service

import com.snuti.exparchiveserver.lecture.dto.TagCreateRequest
import com.snuti.exparchiveserver.lecture.dto.TagResponse
import com.snuti.exparchiveserver.lecture.entity.Tag
import com.snuti.exparchiveserver.lecture.repository.LectureTagRepository
import com.snuti.exparchiveserver.lecture.repository.TagRepository
import com.snuti.exparchiveserver.user.repository.UserInterestTagRepository
import com.snuti.exparchiveserver.user.service.UserInterestService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TagService(
    private val tagRepository: TagRepository,
    private val lectureTagRepository: LectureTagRepository,
    private val userInterestTagRepository: UserInterestTagRepository
) {

    @Transactional
    fun createTag(request: TagCreateRequest): TagResponse {
        val name = request.name.trim()

        if (name.isBlank()) {
            throw IllegalArgumentException("Tag name must not be blank")
        }

        val tag = tagRepository.findByName(name)
            ?: tagRepository.save(Tag(name = name))

        return TagResponse(
            id = tag.id!!,
            name = tag.name
        )
    }

    @Transactional(readOnly = true)
    fun getTags(): List<TagResponse> {
        return tagRepository.findAllByOrderByNameAsc()
            .map {
                TagResponse(
                    id = it.id!!,
                    name = it.name
                )
            }
    }

    @Transactional
    fun deleteTag(id: Long) {
        val tag = tagRepository.findById(id)
            .orElseThrow {
                IllegalArgumentException("Tag not found: $id")
            }

        lectureTagRepository.deleteAllByTag_Id(id)
        userInterestTagRepository.deleteAllByTag_Id(id)

        lectureTagRepository.flush()
        userInterestTagRepository.flush()

        tagRepository.delete(tag)
    }
}