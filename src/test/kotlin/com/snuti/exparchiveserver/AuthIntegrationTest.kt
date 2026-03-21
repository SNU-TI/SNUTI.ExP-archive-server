package com.snuti.exparchiveserver


import com.snuti.exparchiveserver.lecture.repository.ArticleRepository
import com.snuti.exparchiveserver.lecture.repository.LectureRepository
import com.snuti.exparchiveserver.lecture.repository.VideoRepository
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
) {

    @BeforeEach
    fun setup() {
        // FK 제약조건 때문에 자식 테이블부터 삭제
        videoRepository.deleteAll()
        articleRepository.deleteAll()
        lectureRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `should register successfully`() {
        // 정상적인 회원가입이 가능해야 한다.

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
        // 비밀번호 길이가 너무 짧으면 400을 반환해야 한다.

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
        // 회원가입 후 로그인하면 accessToken을 반환해야 한다.

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
        // 비밀번호가 틀리면 로그인 실패(401)해야 한다.

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
}