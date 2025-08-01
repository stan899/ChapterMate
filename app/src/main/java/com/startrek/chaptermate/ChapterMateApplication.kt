package com.startrek.chaptermate

import android.app.Application
import androidx.room.Room
import com.startrek.chaptermate.data.AppDatabase

class ChapterMateApplication : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "chaptermate-db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}
