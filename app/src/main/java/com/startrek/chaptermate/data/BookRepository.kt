package com.startrek.chaptermate.data

import android.content.Context
import com.startrek.chaptermate.BookStatus
import kotlinx.coroutines.flow.Flow

class BookRepository(context: Context) {
    private val dao = (context.applicationContext as com.startrek.chaptermate.ChapterMateApplication)
        .database.bookDao()

    /** Returns a Flow of books whose status matches. */
    fun getBooksByStatus(status: BookStatus): Flow<List<BookEntity>> =
        dao.getBooksByStatus(status.name)

    /** Insert or update a BookEntity. */
    suspend fun upsert(book: BookEntity) =
        dao.upsert(book)

    /** Delete a BookEntity. */
    suspend fun delete(book: BookEntity) =
        dao.delete(book)
}
