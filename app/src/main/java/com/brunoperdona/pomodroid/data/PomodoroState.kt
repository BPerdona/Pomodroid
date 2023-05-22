package com.brunoperdona.pomodroid.data

data class PomodoroState(
    var pomodoroStatus: PomodoroStatus = PomodoroStatus.Idle,
    var pomodoroType: PomodoroType = PomodoroType.Pomodoro
)

enum class PomodoroType{
    Pomodoro, Long, Short
}

enum class PomodoroStatus{
    Idle, Started, Stopped
}
