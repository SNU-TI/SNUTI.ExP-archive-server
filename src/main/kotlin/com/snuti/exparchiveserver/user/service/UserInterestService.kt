package com.snuti.exparchiveserver.user.service

import com.snuti.exparchiveserver.lecture.dto.TagResponse
import com.snuti.exparchiveserver.lecture.repository.TagRepository
import com.snuti.exparchiveserver.user.dto.UserInterestUpdateRequest
import com.snuti.exparchiveserver.user.entity.UserInterestTag
import com.snuti.exparchiveserver.user.repository.UserInterestTagRepository
import com.snuti.exparchiveserver.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserInterestService(
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val userInterestTagRepository: UserInterestTagRepository
) {

    @Transactional(readOnly = true)
    fun getInterests(email: String): List<TagResponse> {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")

        return userInterestTagRepository
            .findAllByUser_IdOrderByTag_NameAsc(user.id!!)
            .map { interest ->
                TagResponse(
                    id = interest.tag.id!!,
                    name = interest.tag.name
                )
            }
    }

    @Transactional
    fun replaceInterests(
        email: String,
        request: UserInterestUpdateRequest
    ): List<TagResponse> {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")

        val tagIds = request.tagIds
            .distinct()

        if (tagIds.any { it <= 0 }) {
            throw IllegalArgumentException("올바르지 않은 키워드 ID가 있습니다.")
        }

        val tags = tagRepository.findAllById(tagIds)

        if (tags.size != tagIds.size) {
            throw IllegalArgumentException("존재하지 않는 키워드가 포함되어 있습니다.")
        }

        userInterestTagRepository.deleteAllByUser_Id(user.id!!)
        userInterestTagRepository.flush()

        userInterestTagRepository.saveAll(
            tags.map { tag ->
                UserInterestTag(
                    user = user,
                    tag = tag
                )
            }
        )

        return tags
            .sortedBy { it.name }
            .map { tag ->
                TagResponse(
                    id = tag.id!!,
                    name = tag.name
                )
            }
    }
}