package com.hellow.noteslite.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ConstantValues {

    val BackGroundColor:List<String> =  listOf("#FFFFFF","#ffb6c1","#90EE90","#80e0ff","#fcf486","#8892fc")
    val titleColor:List<String> = listOf("#000000","#ff6e7f","#6ff26f","#30cdff","#fffcd4","#4556ff")
    val subTitleColor:List<String> = listOf("#6F7378","#ffe6e9","#d1f0d1","#bdedfc","#f7eb40","#c1c6f7")
    val PriorityColors:List<String> = listOf("#000000","#3457D5","#E4D00A","#d62828")

    fun dateConvert(date: String): String {

        val pattern = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy HH:mm:ss a")
        val localDateTime = LocalDateTime.parse(date, pattern)
        return localDateTime.format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy HH:mm a"))
            .toString()

    }

}