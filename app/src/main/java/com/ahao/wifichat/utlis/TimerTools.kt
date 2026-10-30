package com.ahao.wifichat.utlis

import java.util.Timer
import java.util.TimerTask

object TimerTools {
    /** 延迟执行一次性任务，[name] 为计时线程名 */
    fun scheduleTimerTask(name: String, timerTask: TimerTask, delay: Long) {
        Timer(name).schedule(timerTask, delay)
    }
}
