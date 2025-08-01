package com.startrek.chaptermate.data

import androidx.room.TypeConverter
import com.startrek.chaptermate.BookStatus

class Converters {
    @TypeConverter
    fun fromStatus(status: BookStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): BookStatus = BookStatus.valueOf(value)
}
