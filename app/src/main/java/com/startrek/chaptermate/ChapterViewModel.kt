package com.startrek.chaptermate

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.startrek.chaptermate.data.BookEntity
import com.startrek.chaptermate.data.BookRepository
import com.startrek.chaptermate.network.RetrofitInstance
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChapterViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = BookRepository(app)

    // Room-backed lists
    val wantToRead: StateFlow<List<BookEntity>> =
        repo.getBooksByStatus(BookStatus.WantToRead)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentlyReading: StateFlow<List<BookEntity>> =
        repo.getBooksByStatus(BookStatus.CurrentlyReading)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _searchResults = mutableStateOf<List<BookEntity>>(emptyList())
    val searchResults: State<List<BookEntity>> get() = _searchResults

    fun searchOnlineBooks(query: String) = viewModelScope.launch {
        val q = query.trim()
        if (q.isBlank()) {
            _searchResults.value = emptyList()
            return@launch
        }

        try {
            val response = RetrofitInstance.api.searchBooks(q)
            val books = response.items?.mapNotNull { item ->
                val v = item.volumeInfo ?: return@mapNotNull null
                BookEntity(
                    title      = v.title ?: "Untitled",
                    author     = v.authors?.joinToString(", ") ?: "Unknown",
                    pagesRead  = 0,
                    totalPages = v.pageCount ?: 0,
                    status     = BookStatus.WantToRead
                )
            } ?: emptyList()

            _searchResults.value = books
        } catch (e: Exception) {
            _searchResults.value = emptyList()
        }
    }

    fun addBook(
        title: String,
        author: String,
        pagesRead: Int,
        totalPages: Int
    ) = viewModelScope.launch {
        repo.upsert(
            BookEntity(
                title = title,
                author = author,
                pagesRead = pagesRead,
                totalPages = totalPages,
                status = BookStatus.WantToRead
            )
        )
    }

    fun updateProgress(book: BookEntity, newRead: Int) = viewModelScope.launch {
        val updated = book.copy(
            pagesRead = newRead,
            status = if (newRead > 0) BookStatus.CurrentlyReading else BookStatus.WantToRead
        )
        repo.upsert(updated)
    }
}
