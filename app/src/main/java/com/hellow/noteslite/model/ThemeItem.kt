package com.hellow.noteslite.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class ThemeItem(
    val editTextColor: String,
    val hintTextColor: String,
    val backGroundColor: String,
    val toolBarColor: String,
    val timeTextColor:String
) : Parcelable