package com.snuti.exparchiveserver


import com.snuti.exparchiveserver.auth.jwt.JwtTokenProvider
import com.snuti.exparchiveserver.lecture.dto.ArticleBlockRequest
import com.snuti.exparchiveserver.lecture.dto.CreateArticleRequest
import com.snuti.exparchiveserver.lecture.dto.UpdateArticleRequest
import com.snuti.exparchiveserver.lecture.entity.Article
import com.snuti.exparchiveserver.lecture.entity.ArticleBlock
import com.snuti.exparchiveserver.lecture.entity.ArticleBlockType
import com.snuti.exparchiveserver.lecture.entity.Lecture
import com.snuti.exparchiveserver.lecture.entity.LectureStatus
import com.snuti.exparchiveserver.lecture.repository.ArticleRepository
import com.snuti.exparchiveserver.lecture.repository.LectureRepository
import com.snuti.exparchiveserver.lecture.repository.VideoRepository
import com.snuti.exparchiveserver.support.TestImageStorageConfig
import com.snuti.exparchiveserver.user.entity.Role
import com.snuti.exparchiveserver.user.entity.User
import com.snuti.exparchiveserver.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestImageStorageConfig::class)
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

        val article = Article(
            lecture = lecture,
            articleTitle = "Initial Article",
            author = "Student Writer"
        )

        val block1 = ArticleBlock(
            article = article,
            type = ArticleBlockType.TEXT,
            orderIndex = 0,
            textContent = "# Summary"
        )

        val block2 = ArticleBlock(
            article = article,
            type = ArticleBlockType.TEXT,
            orderIndex = 1,
            textContent = "Initial markdown"
        )

        article.replaceBlocks(listOf(block1, block2))

        val savedArticle = articleRepository.save(article)
        articleId = savedArticle.id!!

        userToken = login("user@snu.ac.kr", "password1234")
        adminToken = login("admin@snu.ac.kr", "password1234")

        assertEquals("USER", jwtTokenProvider.parseRole(userToken))
        assertEquals("ADMIN", jwtTokenProvider.parseRole(adminToken))
    }

    @Test
    fun `should retrieve article`() {
        mvc.perform(
            get("/articles/$articleId")
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(articleId))
            .andExpect(jsonPath("$.articleTitle").value("Initial Article"))
            .andExpect(jsonPath("$.author").value("Student Writer"))
            .andExpect(jsonPath("$.blocks[0].type").value("TEXT"))
            .andExpect(jsonPath("$.blocks[0].textContent").value("# Summary"))
            .andExpect(jsonPath("$.blocks[1].type").value("TEXT"))
            .andExpect(jsonPath("$.blocks[1].textContent").value("Initial markdown"))
    }

    @Test
    fun `should create article under lecture`() {
        val request = CreateArticleRequest(
            articleTitle = "Seminar Summary",
            author = "Admin Writer",
            blocks = listOf(
                ArticleBlockRequest(
                    type = ArticleBlockType.TEXT,
                    orderIndex = 0,
                    textContent = "# Summary"
                ),
                ArticleBlockRequest(
                    type = ArticleBlockType.IMAGE,
                    orderIndex = 1,
                    clientImageKey = "img-1"
                ),
                ArticleBlockRequest(
                    type = ArticleBlockType.TEXT,
                    orderIndex = 2,
                    textContent = "Seminar 내용"
                )
            )
        )

        val requestPart = MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            mapper.writeValueAsBytes(request)
        )

        val imagePart = MockMultipartFile(
            "img-1",
            "sample.png",
            MediaType.IMAGE_PNG_VALUE,
            "fake-image-content".toByteArray()
        )

        mvc.perform(
            multipart("/admin/lectures/$lectureId/articles")
                .file(requestPart)
                .file(imagePart)
                .header("Authorization", "Bearer $adminToken")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.lectureId").value(lectureId))
            .andExpect(jsonPath("$.articleTitle").value("Seminar Summary"))
            .andExpect(jsonPath("$.author").value("Admin Writer"))
            .andExpect(jsonPath("$.blocks[0].type").value("TEXT"))
            .andExpect(jsonPath("$.blocks[0].textContent").value("# Summary"))
            .andExpect(jsonPath("$.blocks[1].type").value("IMAGE"))
            .andExpect(jsonPath("$.blocks[1].imageUrl").exists())
            .andExpect(jsonPath("$.blocks[1].originalFileName").value("sample.png"))
            .andExpect(jsonPath("$.blocks[2].type").value("TEXT"))
            .andExpect(jsonPath("$.blocks[2].textContent").value("Seminar 내용"))
    }

    @Test
    fun `should forbid normal user from creating article under lecture`() {
        val request = CreateArticleRequest(
            articleTitle = "Forbidden Article",
            author = "User Writer",
            blocks = listOf(
                ArticleBlockRequest(
                    type = ArticleBlockType.TEXT,
                    orderIndex = 0,
                    textContent = "# Denied"
                )
            )
        )

        val requestPart = MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            mapper.writeValueAsBytes(request)
        )

        mvc.perform(
            multipart("/admin/lectures/$lectureId/articles")
                .file(requestPart)
                .header("Authorization", "Bearer $userToken")
                .contentType(MediaType.MULTIPART_FORM_DATA)
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
    fun `should update article`() {
        val request = UpdateArticleRequest(
            articleTitle = "Updated Title",
            author = "Updated Author",
            blocks = listOf(
                ArticleBlockRequest(
                    type = ArticleBlockType.TEXT,
                    orderIndex = 0,
                    textContent = "수정된 내용"
                )
            )
        )

        val requestPart = MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            mapper.writeValueAsBytes(request)
        )

        mvc.perform(
            multipart("/admin/articles/$articleId")
                .file(requestPart)
                .with { it.method = "PUT"; it }
                .header("Authorization", "Bearer $adminToken")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.articleTitle").value("Updated Title"))
            .andExpect(jsonPath("$.blocks[0].textContent").value("수정된 내용"))
    }

    @Test
    fun `should delete article`() {
        mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/admin/articles/$articleId")
                .header("Authorization", "Bearer $adminToken")
        )
            .andExpect(status().isNoContent)

        mvc.perform(
            get("/admin/articles/$articleId")
                .header("Authorization", "Bearer $userToken")
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `should fail when TEXT block has no content`() {
        val request = CreateArticleRequest(
            articleTitle = "Invalid Article",
            author = "Test",
            blocks = listOf(
                ArticleBlockRequest(
                    type = ArticleBlockType.TEXT,
                    orderIndex = 0,
                    textContent = null
                )
            )
        )

        val requestPart = MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            mapper.writeValueAsBytes(request)
        )

        mvc.perform(
            multipart("/admin/lectures/$lectureId/articles")
                .file(requestPart)
                .header("Authorization", "Bearer $adminToken")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().is4xxClientError)
    }
}