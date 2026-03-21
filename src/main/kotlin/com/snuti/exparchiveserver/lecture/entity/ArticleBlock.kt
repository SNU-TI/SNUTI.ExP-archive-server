package com.snuti.exparchiveserver.lecture.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "article_blocks")
class ArticleBlock(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    var article: Article,

    @Enumerated(EnumType.STRING)
    @Column(name = "block_type", nullable = false, length = 20)
    var type: ArticleBlockType,

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int,

    @Column(name = "text_content", columnDefinition = "LONGTEXT")
    var textContent: String? = null,

    @Column(name = "image_key", length = 500)
    var imageKey: String? = null,

    @Column(name = "original_file_name", length = 255)
    var originalFileName: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    fun validate() {
        when (type) {
            ArticleBlockType.TEXT -> {
                require(!textContent.isNullOrBlank()) { "TEXT block must have textContent" }
                require(imageKey.isNullOrBlank()) { "TEXT block must not have imageKey" }
            }

            ArticleBlockType.IMAGE -> {
                require(!imageKey.isNullOrBlank()) { "IMAGE block must have imageKey" }
                require(textContent.isNullOrBlank()) { "IMAGE block must not have textContent" }
            }
        }
    }
}

enum class ArticleBlockType {
    TEXT,
    IMAGE
}