package com.ahao.wifichat.misc

import com.ahao.wifichat.event.ErrorListener

/** 向监听器分发错误信息（最终展示给用户） */
class ErrorHandler {
    private val listeners = mutableListOf<ErrorListener>()

    fun showError(errorMsg: String) {
        listeners.forEach { it.errorReported(errorMsg) }
    }

    fun showCriticalError(criticalErrorMsg: String) {
        listeners.forEach { it.criticalErrorReported(criticalErrorMsg) }
    }

    fun addErrorListener(listener: ErrorListener) {
        listeners += listener
    }

    fun removeErrorListener(listener: ErrorListener) {
        listeners -= listener
    }
}
