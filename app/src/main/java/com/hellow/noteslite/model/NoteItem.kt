package com.hellow.noteslite.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import org.jetbrains.annotations.NotNull
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Parcelize
@Entity(tableName = "Notes")
data class NoteItem(

    @NotNull
    @PrimaryKey()
    val id:String,
    var title:String = "",
    var descriptionText: String = "",
    var description: List<NoteSubItem> = listOf(NoteSubItem(0,NoteSubItemType.String,false,"")),
    var backgroundColor:Int = 0,
    var priorityColor:Int = 0,
): Parcelable
