package com.snuti.exparchiveserver.video

import com.snuti.exparchiveserver.auth.jwt.JwtTokenProvider
import com.snuti.exparchiveserver.lecture.entity.Lecture
import com.snuti.exparchiveserver.lecture.entity.LectureStatus
import com.snuti.exparchiveserver.lecture.repository.ArticleRepository
import com.snuti.exparchiveserver.lecture.repository.LectureRepository
import com.snuti.exparchiveserver.lecture.repository.VideoRepository
import com.snuti.exparchiveserver.user.entity.Role
import com.snuti.exparchiveserver.user.entity.User
import com.snuti.exparchiveserver.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class VideoIntegrationTest
@Autowired
constructor(
    private val mvc: MockMvc,
    private val mapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val lectureRepository: LectureRepository,
    private val articleRepository: ArticleRepository,
    private val videoRepository: VideoRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
) {

    private lateinit var userToken: String
    private lateinit var adminToken: String

    private var lectureId: Long = 0L

    @BeforeEach
    fun setup() {
        videoRepository.deleteAll()
        articleRepository.deleteAll()
        lectureRepository.deleteAll()
        userRepository.deleteAll()

        val normalUser = userRepository.save(
            User(
                email = "user@snu.ac.kr",
                passwordHash = passwordEncoder.encode("password1234")!!,
                role = Role.USER
            )
        )

        val adminUser = userRepository.save(
            User(
                email = "admin@snu.ac.kr",
                passwordHash = passwordEncoder.encode("password1234")!!,
                role = Role.ADMIN
            )
        )

        val lecture = lectureRepository.save(
            Lecture(
                title = "Video Test Lecture",
                lectureDate = LocalDateTime.of(2026, 2, 20, 14, 0),
                location = "Room 301",
                lectureSummary = "Lecture for video tests",
                lecturerName = "Prof. Choi",
                topic = "Video",
                status = LectureStatus.PUBLISHED,
                createdBy = adminUser
            )
        )

        lectureId = lecture.id!!

        userToken = login("user@snu.ac.kr", "password1234")
        adminToken = login("admin@snu.ac.kr", "password1234")

        assertEquals("USER", jwtTokenProvider.parseRole(userToken))
        assertEquals("ADMIN", jwtTokenProvider.parseRole(adminToken))
    }

    @Test
    fun `should create video under lecture`() {
        val request = mapOf(
            "videoUrl" to "https://example.com/new-video.mp4",
            "caption" to "새 영상"
        )

        mvc.perform(
            post("/admin/lectures/$lectureId/videos")
                .header("Authorization", "Bearer $adminToken")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.lectureId").value(lectureId))
            .andExpect(jsonPath("$.videoUrl").value("https://example.com/new-video.mp4"))
            .andExpect(jsonPath("$.caption").value("새 영상"))
    }

    @Test
    fun `should forbid normal user from creating video under lecture`() {
        val request = mapOf(
            "videoUrl" to "https://example.com/forbidden-video.mp4",
            "caption" to "권한 없음"
        )

        mvc.perform(
            post("/admin/lectures/$lectureId/videos")
                .header("Authorization", "Bearer $userToken")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should include created video in lecture detail`() {
        val request = mapOf(
            "videoUrl" to "https://example.com/detail-video.mp4",
            "caption" to "상세 조회 영상"
        )

        mvc.perform(
            post("/admin/lectures/$lectureId/videos")
                .header("Authorization", "Bearer $adminToken")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)

        mvc.perform(
            get("/lectures/$lectureId")
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.videos[0].videoUrl").value("https://example.com/detail-video.mp4"))
            .andExpect(jsonPath("$.videos[0].caption").value("상세 조회 영상"))
    }

    private fun login(email: String, password: String): String {
        val request = mapOf(
            "email" to email,
            "password" to password
        )

        val result = mvc.perform(
            post("/auth/login")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andReturn()

        return mapper.readTree(result.response.contentAsString)["accessToken"].asText()
    }
}