package com.startrek.chaptermate.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.startrek.chaptermate.BookStatus

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,              // Room will auto-increment this
    val title: String,
    val author: String,
    val pagesRead: Int,
    val totalPages: Int,
    val status: BookStatus        // Using your existing enum
)
