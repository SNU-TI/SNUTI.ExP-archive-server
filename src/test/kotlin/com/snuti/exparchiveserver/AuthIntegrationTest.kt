package com.snuti.exparchiveserver


import com.snuti.exparchiveserver.auth.service.EmailService
import com.snuti.exparchiveserver.lecture.repository.ArticleRepository
import com.snuti.exparchiveserver.lecture.repository.LectureRepository
import com.snuti.exparchiveserver.lecture.repository.VideoRepository
import com.snuti.exparchiveserver.user.repository.EmailVerificationRepository
import com.snuti.exparchiveserver.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthIntegrationTest
@Autowired
constructor(
    private val mvc: MockMvc,
    private val mapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val lectureRepository: LectureRepository,
    private val articleRepository: ArticleRepository,
    private val videoRepository: VideoRepository,
    private val emailVerificationRepository: EmailVerificationRepository,
) {
    @BeforeEach
    fun setup() {
        videoRepository.deleteAll()
        articleRepository.deleteAll()
        lectureRepository.deleteAll()
        emailVerificationRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `should register successfully`() {
        val request = mapOf(
            "email" to "user1@snu.ac.kr",
            "password" to "password1234"
        )

        mvc.perform(
            post("/auth/register")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.accessToken").exists())
    }

    @Test
    fun `should return 400 when password is too short`() {
        val request = mapOf(
            "email" to "user2@snu.ac.kr",
            "password" to "123"
        )

        mvc.perform(
            post("/auth/register")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should login successfully`() {
        val register = mapOf(
            "email" to "user3@snu.ac.kr",
            "password" to "password1234"
        )

        mvc.perform(
            post("/auth/register")
                .content(mapper.writeValueAsString(register))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated)

        val login = mapOf(
            "email" to "user3@snu.ac.kr",
            "password" to "password1234"
        )

        mvc.perform(
            post("/auth/login")
                .content(mapper.writeValueAsString(login))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
    }

    @Test
    fun `should return 401 when password is incorrect`() {
        val register = mapOf(
            "email" to "user4@snu.ac.kr",
            "password" to "password1234"
        )

        mvc.perform(
            post("/auth/register")
                .content(mapper.writeValueAsString(register))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated)

        val login = mapOf(
            "email" to "user4@snu.ac.kr",
            "password" to "wrong-password"
        )

        mvc.perform(
            post("/auth/login")
                .content(mapper.writeValueAsString(login))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should send email verification code`() {
        val email = "user5@snu.ac.kr"

        val register = mapOf(
            "email" to email,
            "password" to "password1234"
        )

        val result = mvc.perform(
            post("/auth/register")
                .content(mapper.writeValueAsString(register))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andReturn()

        val accessToken = mapper.readTree(result.response.contentAsString)
            .get("accessToken")
            .asText()

        mvc.perform(
            post("/auth/email/send")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(status().isNoContent)

        val verification = emailVerificationRepository.findByEmail(email)

        assertNotNull(verification)
        if (verification != null) {
            assertTrue(verification.code.length == 6)
        }
    }

    @Test
    fun `should verify email code successfully`() {
        val email = "user6@snu.ac.kr"

        val register = mapOf(
            "email" to email,
            "password" to "password1234"
        )

        val result = mvc.perform(
            post("/auth/register")
                .content(mapper.writeValueAsString(register))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andReturn()

        val accessToken = mapper.readTree(result.response.contentAsString)
            .get("accessToken")
            .asText()

        mvc.perform(
            post("/auth/email/send")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(status().isNoContent)

        val verification = emailVerificationRepository.findByEmail(email)
            ?: throw IllegalStateException("인증번호가 저장되지 않았습니다.")

        val verifyRequest = mapOf(
            "code" to verification.code
        )

        mvc.perform(
            post("/auth/email/verify")
                .header("Authorization", "Bearer $accessToken")
                .content(mapper.writeValueAsString(verifyRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)

        val user = userRepository.findByEmail(email)
            ?: throw IllegalStateException("사용자를 찾을 수 없습니다.")

        assertTrue(user.emailVerified)
    }

    @Test
    fun `should return 400 when email verification code is wrong`() {
        val email = "user7@snu.ac.kr"

        val register = mapOf(
            "email" to email,
            "password" to "password1234"
        )

        val result = mvc.perform(
            post("/auth/register")
                .content(mapper.writeValueAsString(register))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andReturn()

        val accessToken = mapper.readTree(result.response.contentAsString)
            .get("accessToken")
            .asText()

        mvc.perform(
            post("/auth/email/send")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(status().isNoContent)

        val verifyRequest = mapOf(
            "code" to "000000"
        )

        mvc.perform(
            post("/auth/email/verify")
                .header("Authorization", "Bearer $accessToken")
                .content(mapper.writeValueAsString(verifyRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }
}