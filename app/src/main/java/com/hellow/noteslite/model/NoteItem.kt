package com.hellow.noteslite.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "Notes")
data class NoteItem(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    val title:String = "",
    val description:String = "",
   // val date: String = "",
    val backgroundColor:Int = 0,
    val priorityColor:Int = 0,
): Serializable
