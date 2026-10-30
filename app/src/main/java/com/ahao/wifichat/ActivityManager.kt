package com.ahao.wifichat

import android.app.Activity
import java.lang.ref.WeakReference

/** 维护已打开的 Activity 栈（弱引用，不阻止回收） */
class ActivityManager {

    private val activityStack = mutableListOf<WeakReference<Activity>>()

    private fun removeActivity(find: Activity) {
        activityStack.removeAll { ref -> ref.get().let { it == null || it === find } }
    }

    fun popActivity(activity: Activity) = removeActivity(activity)

    /** 按类名查找仍存活的 Activity */
    fun getActivity(className: String): Activity? {
        activityStack.removeAll { it.get() == null }
        return activityStack.firstNotNullOfOrNull { ref ->
            ref.get()?.takeIf { it.javaClass.name == className }
        }
    }

    fun pushActivity(activity: Activity) {
        activityStack += WeakReference(activity)
    }

    fun clear() {
        activityStack.forEach { it.get()?.finish() }
        activityStack.clear()
    }

    /** 结束栈中第一个类型为 [cls] 的 Activity */
    fun finishActivity(cls: Class<*>) {
        val ref = activityStack.firstOrNull { it.get()?.javaClass == cls } ?: return
        ref.get()?.finish()
        activityStack.remove(ref)
    }

    fun finishActivity(activity: Activity?) {
        activity ?: return
        removeActivity(activity)
        activity.finish()
    }
}
