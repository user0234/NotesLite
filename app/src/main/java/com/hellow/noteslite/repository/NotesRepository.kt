package com.hellow.noteslite.repository

import com.hellow.noteslite.database.NotesDataBase
import com.hellow.noteslite.model.NoteItem
import com.hellow.noteslite.model.ThemeItem
import com.hellow.noteslite.utils.ConstantValues

class NotesRepository(
private val dataBase: NotesDataBase
) {
    suspend fun createNote(note: NoteItem) = dataBase.notesDao().insert(note)

    fun getNotes() = dataBase.notesDao().getNotesList()

    suspend fun deleteNote(note: NoteItem) = dataBase.notesDao().delete(note)

    suspend fun updateNote(note: NoteItem) = dataBase.notesDao().update(note)

    fun getTheme(num:Int,nightMode:Boolean):ThemeItem {
        return if(nightMode){
            ConstantValues.getNightModeTheme(num)
        }else{
            ConstantValues.getLightModeTheme(num)
        }
    }

    fun getAllTheme():List<ThemeItem>{
        return  ConstantValues.themeList
    }

}