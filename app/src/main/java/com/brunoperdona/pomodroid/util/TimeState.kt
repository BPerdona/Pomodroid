package com.brunoperdona.pomodroid.util

data class TimeState(
    val seconds: String,
    val minutes: String,
    val hours: String? = null
){
    fun getFormatedTime(): String{
        return if (hours != null){
            "${hours.padStart(2,'0')}:" +
            "${minutes.padStart(2,'0')}:" +
            seconds.padStart(2,'0')
        } else{
            "${minutes.padStart(2,'0')}:" +
            seconds.padStart(2,'0')
        }
    }
}
