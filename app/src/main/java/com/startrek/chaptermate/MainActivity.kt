package com.startrek.chaptermate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.startrek.chaptermate.ui.theme.ChapterMateTheme

class MainActivity : ComponentActivity() {

    private val chapterViewModel: ChapterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChapterMateTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    HomeScreen(chapterViewModel = chapterViewModel)
                }
            }
        }
    }
}
