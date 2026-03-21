package com.snuti.exparchiveserver


import com.snuti.exparchiveserver.auth.jwt.JwtTokenProvider
import com.snuti.exparchiveserver.lecture.entity.Article
import com.snuti.exparchiveserver.lecture.entity.ArticleBlock
import com.snuti.exparchiveserver.lecture.entity.ArticleBlockType
import com.snuti.exparchiveserver.lecture.entity.Lecture
import com.snuti.exparchiveserver.lecture.entity.LectureStatus
import com.snuti.exparchiveserver.lecture.entity.Video
import com.snuti.exparchiveserver.lecture.repository.ArticleRepository
import com.snuti.exparchiveserver.lecture.repository.LectureRepository
import com.snuti.exparchiveserver.lecture.repository.VideoRepository
import com.snuti.exparchiveserver.support.TestImageStorageConfig
import com.snuti.exparchiveserver.user.entity.Role
import com.snuti.exparchiveserver.user.entity.User
import com.snuti.exparchiveserver.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestImageStorageConfig::class)
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
    private var articleId: Long = 0L

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

        val article = Article(
            lecture = lecture,
            articleTitle = "AI Seminar Summary",
            author = "Student Writer"
        )

        val textBlock1 = ArticleBlock(
            article = article,
            type = ArticleBlockType.TEXT,
            orderIndex = 0,
            textContent = "# Summary"
        )

        val imageBlock = ArticleBlock(
            article = article,
            type = ArticleBlockType.IMAGE,
            orderIndex = 1,
            imageKey = "test/article-images/sample.png",
            originalFileName = "sample.png"
        )

        val textBlock2 = ArticleBlock(
            article = article,
            type = ArticleBlockType.TEXT,
            orderIndex = 2,
            textContent = "This is markdown content."
        )

        article.replaceBlocks(listOf(textBlock1, imageBlock, textBlock2))
        lecture.articles.add(article)

        lecture.videos.add(
            Video(
                lecture = lecture,
                videoUrl = "https://example.com/video.mp4",
                caption = "Main Session Video"
            )
        )

        val savedLecture = lectureRepository.save(lecture)
        lectureId = savedLecture.id!!
        articleId = savedLecture.articles.first().id!!

        userToken = login("user@snu.ac.kr", "password1234")
        adminToken = login("admin@snu.ac.kr", "password1234")

        assertEquals("USER", jwtTokenProvider.parseRole(userToken))
        assertEquals("ADMIN", jwtTokenProvider.parseRole(adminToken))
    }

    @Test
    fun `should retrieve lecture list`() {
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
        assertTrue(lectureRepository.findById(lectureId).isPresent)

        mvc.perform(
            get("/lectures/$lectureId")
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(lectureId))
            .andExpect(jsonPath("$.title").value("AI Seminar"))
            .andExpect(jsonPath("$.articles[0].articleTitle").value("AI Seminar Summary"))
            .andExpect(jsonPath("$.articles[0].author").value("Student Writer"))
            .andExpect(jsonPath("$.articles[0].blocks[0].type").value("TEXT"))
            .andExpect(jsonPath("$.articles[0].blocks[0].textContent").value("# Summary"))
            .andExpect(jsonPath("$.articles[0].blocks[1].type").value("IMAGE"))
            .andExpect(jsonPath("$.articles[0].blocks[1].imageUrl").exists())
            .andExpect(jsonPath("$.articles[0].blocks[1].originalFileName").value("sample.png"))
            .andExpect(jsonPath("$.articles[0].blocks[2].type").value("TEXT"))
            .andExpect(jsonPath("$.articles[0].blocks[2].textContent").value("This is markdown content."))
            .andExpect(jsonPath("$.videos[0].videoUrl").value("https://example.com/video.mp4"))
            .andExpect(jsonPath("$.videos[0].caption").value("Main Session Video"))
    }

    @Test
    fun `should create lecture as admin`() {
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

    @Test
    fun `should not retrieve unpublished lecture`() {
        val draftLecture = lectureRepository.save(
            Lecture(
                title = "Draft Lecture",
                lectureDate = LocalDateTime.now(),
                location = "Test",
                lectureSummary = "Test",
                lecturerName = "Test",
                topic = "Test",
                status = LectureStatus.DRAFT,
                createdBy = userRepository.findAll().first()
            )
        )

        mvc.perform(
            get("/lectures/${draftLecture.id}")
                .header("Authorization", "Bearer $userToken")
        )
            .andExpect(status().is4xxClientError)
    }
}