package com.hellow.noteslite.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull


data class ThemeItem(
    val title_color:String ="#000000",
                     val subTitle_color:String ="#6F7378",
                     val backGround_color:String = "#FFFFFF")