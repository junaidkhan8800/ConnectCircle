package com.example.connectcircle.utils

class Constants {

    companion object {

        //Extension Function
        fun String.capitalizeWords(delimiter: String = " ") =
            split(delimiter).joinToString(delimiter) { word ->

                val smallCaseWord = word.lowercase()
                smallCaseWord.replaceFirstChar(Char::titlecaseChar)

            }

    }

}