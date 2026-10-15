package com.snuti.exparchiveserver.user.service

import com.snuti.exparchiveserver.lecture.dto.LectureListItemResponse
import com.snuti.exparchiveserver.lecture.entity.LectureStatus
import com.snuti.exparchiveserver.lecture.repository.LectureRepository
import com.snuti.exparchiveserver.user.entity.UserFavoriteLecture
import com.snuti.exparchiveserver.user.repository.UserFavoriteLectureRepository
import com.snuti.exparchiveserver.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserFavoriteService(
    private val userRepository: UserRepository,
    private val lectureRepository: LectureRepository,
    private val favoriteRepository: UserFavoriteLectureRepository
) {
    @Transactional
    fun addFavorite(email: String, lectureId: Long) {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("User not found: $email")

        val lecture = lectureRepository.findById(lectureId)
            .orElseThrow {
                IllegalArgumentException("Lecture not found: $lectureId")
            }

        require(lecture.status == LectureStatus.PUBLISHED) {
            "Cannot favorite unpublished lecture: $lectureId"
        }

        if (favoriteRepository.existsByUserIdAndLectureId(user.id!!, lectureId)) {
            return
        }

        favoriteRepository.save(
            UserFavoriteLecture(
                user = user,
                lecture = lecture
            )
        )
    }

    @Transactional
    fun removeFavorite(email: String, lectureId: Long) {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("User not found: $email")

        favoriteRepository.deleteByUserIdAndLectureId(
            user.id!!,
            lectureId
        )
    }

    @Transactional(readOnly = true)
    fun isFavorite(email: String, lectureId: Long): Boolean {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("User not found: $email")

        return favoriteRepository.existsByUserIdAndLectureId(
            user.id!!,
            lectureId
        )
    }
}