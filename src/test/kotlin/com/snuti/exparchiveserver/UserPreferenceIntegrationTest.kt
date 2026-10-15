package com.snuti.exparchiveserver

import com.snuti.exparchiveserver.lecture.entity.Lecture
import com.snuti.exparchiveserver.lecture.entity.LectureStatus
import com.snuti.exparchiveserver.lecture.entity.LectureTag
import com.snuti.exparchiveserver.lecture.entity.Tag
import com.snuti.exparchiveserver.lecture.repository.ArticleRepository
import com.snuti.exparchiveserver.lecture.repository.VideoRepository
import com.snuti.exparchiveserver.lecture.repository.LectureRepository
import com.snuti.exparchiveserver.lecture.repository.LectureTagRepository
import com.snuti.exparchiveserver.lecture.repository.TagRepository
import com.snuti.exparchiveserver.user.entity.Role
import com.snuti.exparchiveserver.user.entity.User
import com.snuti.exparchiveserver.user.repository.UserFavoriteLectureRepository
import com.snuti.exparchiveserver.user.repository.UserInterestTagRepository
import com.snuti.exparchiveserver.user.repository.UserRepository
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.snuti.exparchiveserver.support.TestImageStorageConfig
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestImageStorageConfig::class)
class UserPreferenceIntegrationTest @Autowired constructor(
    private val mvc: MockMvc,
    private val mapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val lectureRepository: LectureRepository,
    private val articleRepository: ArticleRepository,
    private val videoRepository: VideoRepository,
    private val lectureTagRepository: LectureTagRepository,
    private val tagRepository: TagRepository,
    private val userInterestTagRepository: UserInterestTagRepository,
    private val userFavoriteLectureRepository: UserFavoriteLectureRepository,
    private val passwordEncoder: PasswordEncoder
) {
    private lateinit var userToken: String
    private var publishedLectureId: Long = 0
    private var draftLectureId: Long = 0
    private var aiTagId: Long = 0
    private var llmTagId: Long = 0

    @BeforeEach
    fun setup() {
        // FK 관계를 고려해 자식 테이블부터 삭제한다.
        userFavoriteLectureRepository.deleteAll()
        userInterestTagRepository.deleteAll()
        lectureTagRepository.deleteAll()
        videoRepository.deleteAll()
        articleRepository.deleteAll()
        lectureRepository.deleteAll()
        tagRepository.deleteAll()
        userRepository.deleteAll()

        val user = userRepository.save(
            User(
                email = "user@snu.ac.kr",
                passwordHash = passwordEncoder.encode("password1234")!!,
                role = Role.USER
            )
        )
        val published = lectureRepository.save(
            lecture("AI Seminar", LectureStatus.PUBLISHED, user)
        )
        val draft = lectureRepository.save(
            lecture("Draft Seminar", LectureStatus.DRAFT, user)
        )
        publishedLectureId = published.id!!
        draftLectureId = draft.id!!

        val ai = tagRepository.save(Tag(name = "AI"))
        val llm = tagRepository.save(Tag(name = "LLM"))
        aiTagId = ai.id!!
        llmTagId = llm.id!!
        lectureTagRepository.saveAll(
            listOf(LectureTag(published, ai), LectureTag(published, llm))
        )
        userToken = login("user@snu.ac.kr", "password1234")
    }

    @Test
    fun `관심 태그를 설정하고 조회하고 해제한다`() {
        replaceInterests(listOf(aiTagId, llmTagId))
            .andExpect(status().isOk)

        mvc.perform(get("/users/me/interests").auth())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))

        replaceInterests(listOf(aiTagId))
            .andExpect(status().isOk)
        mvc.perform(get("/users/me/interests").auth())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("AI"))

        replaceInterests(emptyList()).andExpect(status().isOk)
        mvc.perform(get("/users/me/interests").auth())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `관심 태그가 겹쳐도 공개 강좌를 중복 추천하지 않는다`() {
        replaceInterests(listOf(aiTagId, llmTagId))
            .andExpect(status().isOk)
        mvc.perform(get("/lectures/recommended").auth())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("AI Seminar"))
    }

    @Test
    fun `관심 태그가 없으면 추천 목록이 비어 있다`() {
        mvc.perform(get("/lectures/recommended").auth())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(0))
    }

    @Test
    fun `관심 강좌를 등록하고 목록과 등록 여부를 조회한다`() {
        mvc.perform(post("/users/me/favorites/$publishedLectureId").auth())
            .andExpect(status().is2xxSuccessful)

        mvc.perform(get("/users/me/favorites/$publishedLectureId").auth())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(true))

        mvc.perform(get("/users/me/favorites").auth())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("AI Seminar"))
    }

    @Test
    fun `같은 강좌를 여러 번 등록해도 중복 저장되지 않는다`() {
        repeat(2) {
            mvc.perform(post("/users/me/favorites/$publishedLectureId").auth())
                .andExpect(status().is2xxSuccessful)
        }
        mvc.perform(get("/users/me/favorites").auth())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
    }

    @Test
    fun `관심 강좌를 해제하면 등록 여부가 false가 된다`() {
        mvc.perform(post("/users/me/favorites/$publishedLectureId").auth())
            .andExpect(status().is2xxSuccessful)
        mvc.perform(delete("/users/me/favorites/$publishedLectureId").auth())
            .andExpect(status().is2xxSuccessful)
        mvc.perform(get("/users/me/favorites/$publishedLectureId").auth())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(false))
        mvc.perform(get("/users/me/favorites").auth())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(0))
    }

    @Test
    fun `DRAFT 강좌는 관심 강좌로 등록할 수 없다`() {
        mvc.perform(post("/users/me/favorites/$draftLectureId").auth())
            .andExpect(status().is4xxClientError)
    }

    private fun lecture(title: String, status: LectureStatus, user: User) = Lecture(
        title = title,
        lectureDate = LocalDateTime.of(2026, 2, 1, 10, 0),
        location = "Engineering Hall",
        lectureSummary = "Test summary",
        lecturerName = "Prof. Kim",
        topic = "AI",
        status = status,
        createdBy = user
    )

    private fun org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder.auth() =
        header("Authorization", "Bearer $userToken")

    private fun replaceInterests(tagIds: List<Long>) = mvc.perform(
        put("/users/me/interests")
            .auth()
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(mapOf("tagIds" to tagIds)))
    )

    private fun login(email: String, password: String): String {
        val result = mvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapOf("email" to email, "password" to password)))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andReturn()
        return mapper.readTree(result.response.contentAsString)["accessToken"].asText()
    }
}