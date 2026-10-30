package com.ahao.wifichat.event

/** 错误回调，实现方负责把错误信息展示给用户 */
interface ErrorListener {
    fun errorReported(errorMsg: String)
    fun criticalErrorReported(criticalErrorMsg: String)
}
