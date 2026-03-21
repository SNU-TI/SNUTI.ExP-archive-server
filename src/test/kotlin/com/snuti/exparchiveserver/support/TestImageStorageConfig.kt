package com.snuti.exparchiveserver.support

import com.snuti.exparchiveserver.common.storage.ImageStorageService
import com.snuti.exparchiveserver.common.storage.StoredImage
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@TestConfiguration
class TestImageStorageConfig {

    @Bean
    @Primary
    fun testImageStorageService(): ImageStorageService {
        return object : ImageStorageService {
            override fun store(file: MultipartFile): StoredImage {
                return StoredImage(
                    key = "test/${UUID.randomUUID()}-${file.originalFilename}",
                    originalFileName = file.originalFilename ?: "test.png"
                )
            }

            override fun deleteByKey(key: String) {
                // no-op
            }

            override fun getUrl(key: String): String {
                return "https://test.local/$key"
            }
        }
    }
}