package com.hellow.noteslite.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hellow.noteslite.model.NoteItem

@Dao
interface NotesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note:NoteItem):Long

    @Query("Select * From Notes")
    fun getNotesList(): LiveData<List<NoteItem>>

    @Query("Select * From Notes Where id = :noteId")
    fun getNote(noteId:Int):MutableList<NoteItem>

    @Delete
    suspend fun delete(note: NoteItem)
}