package com.hellow.noteslite.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "Notes")
data class NoteItem(

    @NotNull
    @PrimaryKey()
    val id:String,
    var title:String = "",
    var description:String = "",
    var backgroundColor:Int = 0,
    var priorityColor:Int = 0,
): Serializable
