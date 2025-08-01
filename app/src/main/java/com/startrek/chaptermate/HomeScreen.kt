package com.startrek.chaptermate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.startrek.chaptermate.data.BookEntity

@Composable
fun HomeScreen(chapterViewModel: ChapterViewModel = viewModel()) {
    // UI state
    var selectedTab      by remember { mutableStateOf("Currently Reading") }
    var searchQuery      by remember { mutableStateOf("") }
    var showAddDialog    by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var bookToUpdate     by remember { mutableStateOf<BookEntity?>(null) }

    // Dialog inputs
    var newTitle       by remember { mutableStateOf("") }
    var newAuthor      by remember { mutableStateOf("") }
    var newPagesRead   by remember { mutableStateOf("") }
    var newTotalPages  by remember { mutableStateOf("") }
    var updatedPages   by remember { mutableStateOf("") }

    // Persisted lists from the ViewModel
    val wantList    by chapterViewModel.wantToRead.collectAsState()
    val readingList by chapterViewModel.currentlyReading.collectAsState()

    // Online search results from the ViewModel (observable State)
    val onlineResults by chapterViewModel.searchResults

    // --- ADD NEW BOOK DIALOG ---
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title   = { Text("Add a New Book") },
            text    = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newAuthor,
                        onValueChange = { newAuthor = it },
                        label = { Text("Author") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPagesRead,
                        onValueChange = { newPagesRead = it },
                        label = { Text("Pages Read") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTotalPages,
                        onValueChange = { newTotalPages = it },
                        label = { Text("Total Pages") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val r = newPagesRead.toIntOrNull()  ?: 0
                    val t = newTotalPages.toIntOrNull() ?: 0

                    chapterViewModel.addBook(newTitle, newAuthor, r, t)

                    newTitle      = ""
                    newAuthor     = ""
                    newPagesRead  = ""
                    newTotalPages = ""
                    showAddDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showUpdateDialog && bookToUpdate != null) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title   = { Text("Update Progress") },
            text    = {
                OutlinedTextField(
                    value = updatedPages,
                    onValueChange = { updatedPages = it },
                    label = { Text("Pages Read") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val newRead = updatedPages.toIntOrNull() ?: bookToUpdate!!.pagesRead
                    chapterViewModel.updateProgress(bookToUpdate!!, newRead)

                    bookToUpdate     = null
                    showUpdateDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    bookToUpdate     = null
                    showUpdateDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- MAIN SCREEN CONTENT ---
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // App Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("ChapterMate 📚", style = MaterialTheme.typography.headlineMedium)
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search books...") },
                trailingIcon = {
                    IconButton(onClick = { chapterViewModel.searchOnlineBooks(searchQuery) }) {
                        Icon(Icons.Default.Search, contentDescription = "Go")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        chapterViewModel.searchOnlineBooks(searchQuery)
                    }
                )
            )

            if (onlineResults.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Online results", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    items(items = onlineResults) { book ->
                        OnlineResultCard(
                            title = book.title,
                            author = book.author,
                            pages = book.totalPages,
                            onAdd = {
                                chapterViewModel.addBook(book.title, book.author, 0, book.totalPages)
                            }
                        )
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }

            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabButton("Currently Reading", selectedTab) { selectedTab = "Currently Reading" }
                TabButton("Want to Read",      selectedTab) { selectedTab = "Want to Read" }
            }

            Spacer(Modifier.height(16.dp))

            val displayed = (if (selectedTab == "Currently Reading") readingList else wantList)
                .filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                            it.author.contains(searchQuery, ignoreCase = true)
                }

            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (displayed.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (selectedTab == "Currently Reading")
                                "No books in your\n“Currently Reading” list yet."
                            else
                                "No books in your\n“Want to Read” list yet.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            if (selectedTab == "Currently Reading") {
                                selectedTab = "Want to Read"
                            } else {
                                showAddDialog = true
                            }
                        }) {
                            Text("➕ Add New Book")
                        }
                    }
                } else {
                    LazyColumn {
                        items(items = displayed) { book ->
                            BookCard(book) {
                                bookToUpdate = it
                                updatedPages = it.pagesRead.toString()
                                showUpdateDialog = true
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Bottom “Add” button on WantToRead when list non-empty
            if (selectedTab == "Want to Read" && wantList.isNotEmpty()) {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("➕ Add New Book")
                }
            }
        }
    }
}

@Composable
private fun OnlineResultCard(
    title: String,
    author: String,
    pages: Int,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("📖 $title", style = MaterialTheme.typography.titleMedium)
            Text("by $author")
            if (pages > 0) {
                Spacer(Modifier.height(4.dp))
                Text("Pages: $pages")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onAdd) {
                Text("Add to Want to Read")
            }
        }
    }
}

@Composable
fun BookCard(book: BookEntity, onUpdate: (BookEntity) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📖 ${book.title}", style = MaterialTheme.typography.titleMedium)
            Text("by ${book.author}")
            Spacer(Modifier.height(8.dp))
            Text("Progress: ${book.pagesRead} / ${book.totalPages} pages")
            Spacer(Modifier.height(8.dp))
            Button(onClick = { onUpdate(book) }) {
                Text("Update Progress")
            }
        }
    }
}

@Composable
fun TabButton(text: String, selectedTab: String, onClick: () -> Unit) {
    val isSelected  = text == selectedTab
    val bgColor     = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
    val fgColor     = if (isSelected) Color.White                          else Color.Gray
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray

    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = bgColor,
            contentColor   = fgColor
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(text)
    }
}
