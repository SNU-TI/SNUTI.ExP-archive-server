package com.snuti.exparchiveserver

import com.snuti.exparchiveserver.auth.dto.EmailVerificationPurpose
import com.snuti.exparchiveserver.auth.service.EmailService
import com.snuti.exparchiveserver.lecture.repository.ArticleRepository
import com.snuti.exparchiveserver.lecture.repository.LectureRepository
import com.snuti.exparchiveserver.lecture.repository.VideoRepository
import com.snuti.exparchiveserver.user.repository.EmailVerificationRepository
import com.snuti.exparchiveserver.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthIntegrationTest @Autowired constructor(
    private val mvc: MockMvc,
    private val mapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val lectureRepository: LectureRepository,
    private val articleRepository: ArticleRepository,
    private val videoRepository: VideoRepository,
    private val emailVerificationRepository: EmailVerificationRepository
) {

    /*
     * 테스트 중 실제 메일이 발송되지 않도록
     * EmailService 빈을 Mockito mock으로 교체한다.
     */
    @MockitoBean
    lateinit var emailService: EmailService

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
        register(
            email = "user3@snu.ac.kr",
            password = "password1234"
        )

        val loginRequest = mapOf(
            "email" to "user3@snu.ac.kr",
            "password" to "password1234"
        )

        mvc.perform(
            post("/auth/login")
                .content(mapper.writeValueAsString(loginRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
    }

    @Test
    fun `should return 401 when password is incorrect`() {
        register(
            email = "user4@snu.ac.kr",
            password = "password1234"
        )

        val loginRequest = mapOf(
            "email" to "user4@snu.ac.kr",
            "password" to "wrong-password"
        )

        mvc.perform(
            post("/auth/login")
                .content(mapper.writeValueAsString(loginRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should send signup email verification code`() {
        val email = "user5@snu.ac.kr"
        val accessToken = register(email, "password1234")

        mvc.perform(
            post("/auth/email/send")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(status().isNoContent)

        val verification =
            emailVerificationRepository.findByEmailAndPurpose(
                email,
                EmailVerificationPurpose.SIGN_UP
            )

        assertNotNull(verification)
        assertTrue(verification!!.code.length == 6)
        assertTrue(verification.code.all { it.isDigit() })
    }

    @Test
    fun `should verify signup email code successfully`() {
        val email = "user6@snu.ac.kr"
        val accessToken = register(email, "password1234")

        mvc.perform(
            post("/auth/email/send")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(status().isNoContent)

        val verification =
            emailVerificationRepository.findByEmailAndPurpose(
                email,
                EmailVerificationPurpose.SIGN_UP
            ) ?: throw IllegalStateException(
                "회원가입 인증번호가 저장되지 않았습니다."
            )

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
    fun `should return 400 when signup verification code is wrong`() {
        val email = "user7@snu.ac.kr"
        val accessToken = register(email, "password1234")

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

    /*
     * 비밀번호 재설정 인증번호 발송 테스트
     */
    @Test
    fun `should send password reset verification code`() {
        val email = "reset1@snu.ac.kr"

        register(email, "password1234")

        val request = mapOf(
            "email" to email
        )

        mvc.perform(
            post("/auth/password/reset/send")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)

        val verification =
            emailVerificationRepository.findByEmailAndPurpose(
                email,
                EmailVerificationPurpose.PASSWORD_RESET
            )

        assertNotNull(verification)
        assertTrue(verification!!.code.length == 6)
        assertTrue(verification.code.all { it.isDigit() })
    }

    /*
     * 비밀번호 재설정 인증번호 확인 테스트
     */
    @Test
    fun `should verify password reset code successfully`() {
        val email = "reset2@snu.ac.kr"

        register(email, "password1234")
        sendPasswordResetCode(email)

        val verification =
            emailVerificationRepository.findByEmailAndPurpose(
                email,
                EmailVerificationPurpose.PASSWORD_RESET
            ) ?: throw IllegalStateException(
                "비밀번호 재설정 인증번호가 저장되지 않았습니다."
            )

        val request = mapOf(
            "email" to email,
            "code" to verification.code
        )

        mvc.perform(
            post("/auth/password/reset/verify")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)
    }

    /*
     * 비밀번호 재설정 후 새로운 비밀번호로 로그인되는지 확인한다.
     */
    @Test
    fun `should reset password successfully`() {
        val email = "reset3@snu.ac.kr"
        val oldPassword = "password1234"
        val newPassword = "newPassword5678"

        register(email, oldPassword)
        sendPasswordResetCode(email)

        val verification =
            emailVerificationRepository.findByEmailAndPurpose(
                email,
                EmailVerificationPurpose.PASSWORD_RESET
            ) ?: throw IllegalStateException(
                "비밀번호 재설정 인증번호가 저장되지 않았습니다."
            )

        val resetRequest = mapOf(
            "email" to email,
            "code" to verification.code,
            "newPassword" to newPassword
        )

        mvc.perform(
            post("/auth/password/reset")
                .content(mapper.writeValueAsString(resetRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)

        /*
         * 새 비밀번호로 로그인이 성공해야 한다.
         */
        val newLoginRequest = mapOf(
            "email" to email,
            "password" to newPassword
        )

        mvc.perform(
            post("/auth/login")
                .content(mapper.writeValueAsString(newLoginRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())

        /*
         * 사용한 인증번호는 삭제되어야 한다.
         */
        val deletedVerification =
            emailVerificationRepository.findByEmailAndPurpose(
                email,
                EmailVerificationPurpose.PASSWORD_RESET
            )

        assertNull(deletedVerification)
    }

    @Test
    fun `should return 400 when password reset code is wrong`() {
        val email = "reset4@snu.ac.kr"

        register(email, "password1234")
        sendPasswordResetCode(email)

        val request = mapOf(
            "email" to email,
            "code" to "000000",
            "newPassword" to "newPassword5678"
        )

        mvc.perform(
            post("/auth/password/reset")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    /*
     * 회원탈퇴 테스트
     */
    @Test
    fun `should delete logged in user account`() {
        val email = "delete1@snu.ac.kr"
        val accessToken = register(email, "password1234")

        mvc.perform(
            delete("/auth/me")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(status().isNoContent)

        val deletedUser = userRepository.findByEmail(email)

        assertNull(deletedUser)
    }

    /*
     * 회원탈퇴 후 기존 계정으로 로그인할 수 없어야 한다.
     */
    @Test
    fun `should not login after account deletion`() {
        val email = "delete2@snu.ac.kr"
        val password = "password1234"
        val accessToken = register(email, password)

        mvc.perform(
            delete("/auth/me")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(status().isNoContent)

        val loginRequest = mapOf(
            "email" to email,
            "password" to password
        )

        mvc.perform(
            post("/auth/login")
                .content(mapper.writeValueAsString(loginRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
    }

    /*
     * 반복되는 회원가입 코드를 줄이기 위한 테스트 헬퍼.
     * 회원가입 후 accessToken을 반환한다.
     */
    private fun register(
        email: String,
        password: String
    ): String {
        val request = mapOf(
            "email" to email,
            "password" to password
        )

        val result = mvc.perform(
            post("/auth/register")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.accessToken").exists())
            .andReturn()

        return mapper.readTree(result.response.contentAsString)
            .get("accessToken")
            .asText()
    }

    /*
     * 비밀번호 재설정 인증번호 발송 헬퍼.
     */
    private fun sendPasswordResetCode(email: String) {
        val request = mapOf(
            "email" to email
        )

        mvc.perform(
            post("/auth/password/reset/send")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)
    }
}