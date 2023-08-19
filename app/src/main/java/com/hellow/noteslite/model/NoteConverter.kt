package com.hellow.noteslite.model

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


@ProvidedTypeConverter
class NoteConverter {

        private val gson = Gson()
        @TypeConverter
        fun fromListNoteDescription(value: List<NoteSubItem>): String {
            return gson.toJson(value)
        }

        @TypeConverter
        fun toListNoteDescription(value: String): List<NoteSubItem> {
            return gson.fromJson(value, object : TypeToken<List<NoteSubItem>>() {
            }.type)
        }
}