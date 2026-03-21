package com.snuti.exparchiveserver.lecture.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "articles")
class Article(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    var lecture: Lecture,

    @Column(name = "article_title", nullable = false, length = 200)
    var articleTitle: String,

    @Column(length = 100)
    var author: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @OneToMany(
        mappedBy = "article",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var blocks: MutableList<ArticleBlock> = mutableListOf()

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    fun replaceBlocks(newBlocks: List<ArticleBlock>) {
        blocks.clear()
        blocks.addAll(newBlocks)
        newBlocks.forEach { it.article = this }
    }
}