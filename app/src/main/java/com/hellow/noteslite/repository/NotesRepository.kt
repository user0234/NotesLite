package com.hellow.noteslite.repository

import com.hellow.noteslite.database.NotesDataBase
import com.hellow.noteslite.model.NoteItem

class NotesRepository(
private val dataBase: NotesDataBase
) {
    suspend fun createNote(note: NoteItem) = dataBase.notesDao().insert(note)

    fun getNotes() = dataBase.notesDao().getNotesList()

    suspend fun deleteNote(note: NoteItem) = dataBase.notesDao().delete(note)

    suspend fun updateNote(note: NoteItem) = dataBase.notesDao().update(note)


}