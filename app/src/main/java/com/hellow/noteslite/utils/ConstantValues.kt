package com.hellow.noteslite.utils

import androidx.appcompat.app.AppCompatDelegate.NightMode
import com.hellow.noteslite.model.ThemeItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ConstantValues {

    val BackGroundColor:List<String> =  listOf("#ffffff","#fcfcde","#defce0","#def2fc")
    val titleColor:List<String> = listOf("#000000","#c4c452","#50bf58","#4f9cc2")
    val subTitleColor:List<String> = listOf("#3d3d3d","#c7c77d","#8abf8e","#82adc2")
    val toolBarColor:List<String> = listOf("ababab","#fafae8","#f0fff1","#edf6fa")
    val PriorityColors:List<String> = listOf("#000000","#3457D5","#E4D00A","#d62828")
    val NightModeDefaultTheme:ThemeItem = ThemeItem("#f2f5f3","#cdd1ce","#0f0f0f","#4f4e4e")
    fun dateConvert(date: String): String {
        val pattern = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy HH:mm:ss a")
        val localDateTime = LocalDateTime.parse(date, pattern)
        return localDateTime.format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy HH:mm a"))
            .toString()

    }

}