package com.brunoperdona.pomodroid.data

data class TimeState(
    val seconds: String,
    val minutes: String,
    val hours: String
){
    fun getFormatedTime(): String{
        return if (hours.isBlank() || hours == "0" || hours == "00"){
            "${minutes.padStart(2,'0')}:" +
            seconds.padStart(2,'0')
        } else{
            "${hours.padStart(2,'0')}:" +
            "${minutes.padStart(2,'0')}:" +
            seconds.padStart(2,'0')
        }
    }
}
