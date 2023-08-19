package com.hellow.noteslite.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class NoteSubItem(
    var id:Int,
    var type:NoteSubItemType,
    var checkBox:Boolean, // false for string type item by default
    var textValue:String
 ) : Parcelable
