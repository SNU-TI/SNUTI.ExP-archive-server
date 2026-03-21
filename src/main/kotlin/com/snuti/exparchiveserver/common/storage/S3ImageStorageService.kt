package com.snuti.exparchiveserver.common.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.time.LocalDate
import java.util.UUID

@Service
class S3ImageStorageService(
    private val s3Client: S3Client,
    @Value("\${cloud.aws.s3.bucket}") private val bucket: String,
    @Value("\${app.cdn.base-url}") private val baseUrl: String
) : ImageStorageService {

    private val allowedExtensions = setOf("jpg", "jpeg", "png", "webp", "gif")

    override fun store(file: MultipartFile): StoredImage {
        require(!file.isEmpty) { "Empty file is not allowed" }

        val contentType = file.contentType ?: ""
        require(contentType.startsWith("image/")) { "Only image files are allowed" }

        val originalFilename = StringUtils.cleanPath(file.originalFilename ?: "image")
        val extension = extractExtension(originalFilename)
        require(extension in allowedExtensions) { "Unsupported image extension: $extension" }

        val key = generateKey(extension)

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .contentLength(file.size)
            .build()

        file.inputStream.use { inputStream ->
            s3Client.putObject(
                putObjectRequest,
                RequestBody.fromInputStream(inputStream, file.size)
            )
        }

        return StoredImage(
            key = key,
            originalFileName = originalFilename
        )
    }

    override fun deleteByKey(key: String) {
        val deleteRequest = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        s3Client.deleteObject(deleteRequest)
    }

    override fun getUrl(key: String): String {
        return "${baseUrl.removeSuffix("/")}/$key"
    }

    private fun extractExtension(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "")
        return if (ext.isBlank()) "bin" else ext.lowercase()
    }

    private fun generateKey(extension: String): String {
        val today = LocalDate.now()
        return "article-images/${today.year}/${today.monthValue}/${today.dayOfMonth}/${UUID.randomUUID()}.$extension"
    }
}