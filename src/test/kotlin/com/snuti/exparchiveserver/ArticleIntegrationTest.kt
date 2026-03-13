package com.snuti.exparchiveserver.article

import com.snuti.exparchiveserver.auth.jwt.JwtTokenProvider
import com.snuti.exparchiveserver.lecture.entity.Article
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
class ArticleIntegrationTest
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
    private var articleId: Long = 0L

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

        val lecture = lectureRepository.save(
            Lecture(
                title = "Article Test Lecture",
                lectureDate = LocalDateTime.of(2026, 2, 10, 15, 0),
                location = "Room 201",
                lectureSummary = "Lecture for article tests",
                lecturerName = "Prof. Park",
                topic = "Backend",
                status = LectureStatus.PUBLISHED,
                createdBy = adminUser
            )
        )

        lectureId = lecture.id!!

        val article = articleRepository.save(
            Article(
                lecture = lecture,
                articleTitle = "Initial Article",
                author = "Student Writer",
                content = "# Summary\nInitial markdown"
            )
        )

        articleId = article.id!!

        userToken = login("user@snu.ac.kr", "password1234")
        adminToken = login("admin@snu.ac.kr", "password1234")

        // 토큰 role 검증
        assertEquals("USER", jwtTokenProvider.parseRole(userToken))
        assertEquals("ADMIN", jwtTokenProvider.parseRole(adminToken))
    }

    @Test
    fun `should retrieve article`() {
        // 로그인한 일반 사용자는 기사 단건을 조회할 수 있어야 한다.

        mvc.perform(
            get("/articles/$articleId")
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(articleId))
            .andExpect(jsonPath("$.articleTitle").value("Initial Article"))
            .andExpect(jsonPath("$.content").value("# Summary\nInitial markdown"))
    }

    @Test
    fun `should create article under lecture`() {
        // ADMIN 사용자는 특정 강연에 article을 추가할 수 있어야 한다.

        val request = mapOf(
            "articleTitle" to "Seminar Summary",
            "author" to "Admin Writer",
            "content" to "# Summary\nSeminar 내용"
        )

        mvc.perform(
            post("/admin/lectures/$lectureId/articles")
                .header("Authorization", "Bearer $adminToken")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.lectureId").value(lectureId))
            .andExpect(jsonPath("$.articleTitle").value("Seminar Summary"))
    }

    @Test
    fun `should forbid normal user from creating article under lecture`() {
        // 일반 사용자는 관리자 article 생성 API를 호출할 수 없어야 한다.

        val request = mapOf(
            "articleTitle" to "Forbidden Article",
            "author" to "User Writer",
            "content" to "# Denied\nNo permission"
        )

        mvc.perform(
            post("/admin/lectures/$lectureId/articles")
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