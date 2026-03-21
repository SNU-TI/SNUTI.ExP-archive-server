package com.snuti.exparchiveserver.common.storage

import org.springframework.web.multipart.MultipartFile

interface ImageStorageService {
    fun store(file: MultipartFile): StoredImage
    fun deleteByKey(key: String)
    fun getUrl(key: String): String
}

data class StoredImage(
    val key: String,
    val originalFileName: String
)