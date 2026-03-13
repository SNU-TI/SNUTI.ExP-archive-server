package com.snuti.exparchiveserver.lecture


import com.snuti.exparchiveserver.auth.jwt.JwtTokenProvider
import com.snuti.exparchiveserver.lecture.entity.Article
import com.snuti.exparchiveserver.lecture.entity.Lecture
import com.snuti.exparchiveserver.lecture.entity.LectureStatus
import com.snuti.exparchiveserver.lecture.entity.Video
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
class LectureIntegrationTest
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
        // FK 제약조건 때문에 자식 테이블부터 삭제
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

        val lecture = Lecture(
            title = "AI Seminar",
            lectureDate = LocalDateTime.of(2026, 2, 1, 10, 0),
            location = "Engineering Hall",
            lectureSummary = "Introduction to AI",
            lecturerName = "Prof. Kim",
            topic = "LLM",
            status = LectureStatus.PUBLISHED,
            createdBy = normalUser
        )

        lecture.articles.add(
            Article(
                lecture = lecture,
                articleTitle = "AI Seminar Summary",
                author = "Student Writer",
                content = "# Summary\nThis is markdown content."
            )
        )

        lecture.videos.add(
            Video(
                lecture = lecture,
                videoUrl = "https://example.com/video.mp4",
                caption = "Main Session Video"
            )
        )

        lectureId = lectureRepository.save(lecture).id!!

        userToken = login("user@snu.ac.kr", "password1234")
        adminToken = login("admin@snu.ac.kr", "password1234")

        // 토큰 role 검증
        assertEquals("USER", jwtTokenProvider.parseRole(userToken))
        assertEquals("ADMIN", jwtTokenProvider.parseRole(adminToken))
    }

    @Test
    fun `should retrieve lecture list`() {
        // 로그인한 일반 사용자는 강연 목록을 조회할 수 있어야 한다.

        mvc.perform(
            get("/lectures")
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].title").value("AI Seminar"))
            .andExpect(jsonPath("$.content[0].topic").value("LLM"))
    }

    @Test
    fun `should retrieve lecture detail`() {
        // 로그인한 일반 사용자는 특정 강연의 상세 정보를 조회할 수 있어야 한다.

        mvc.perform(
            get("/lectures/$lectureId")
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(lectureId))
            .andExpect(jsonPath("$.title").value("AI Seminar"))
            .andExpect(jsonPath("$.articles[0].articleTitle").value("AI Seminar Summary"))
            .andExpect(jsonPath("$.videos[0].videoUrl").value("https://example.com/video.mp4"))
    }

    @Test
    fun `should create lecture as admin`() {
        // ADMIN 사용자는 강연 생성 API를 호출할 수 있어야 한다.

        val request = mapOf(
            "title" to "Backend Seminar",
            "lectureDate" to "2026-03-01T14:00:00",
            "location" to "Room 301",
            "lectureSummary" to "Spring Boot Intro",
            "lecturerName" to "Professor Lee",
            "topic" to "Backend",
            "status" to "PUBLISHED"
        )

        mvc.perform(
            post("/admin/lectures")
                .header("Authorization", "Bearer $adminToken")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("Backend Seminar"))
            .andExpect(jsonPath("$.topic").value("Backend"))
    }

    @Test
    fun `should forbid normal user from creating lecture`() {
        // 일반 사용자는 관리자 강연 생성 API를 호출할 수 없어야 한다.

        val request = mapOf(
            "title" to "Unauthorized Seminar",
            "lectureDate" to "2026-03-01T14:00:00"
        )

        mvc.perform(
            post("/admin/lectures")
                .header("Authorization", "Bearer $userToken")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
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