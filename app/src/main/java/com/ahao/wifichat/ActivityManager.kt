package com.ahao.wifichat;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Stack;

import android.app.Activity;

public class ActivityManager {

	private final Stack<WeakReference<Activity>> activityStack = new Stack<WeakReference<Activity>>();

	private void removeActivity(Activity find) {
		Iterator<WeakReference<Activity>> iter = activityStack.iterator();
		while (iter.hasNext()) {
			WeakReference<Activity> item = iter.next();

			Activity activity = item.get();
			if (activity == null) {
				iter.remove();
			} else if (activity == find) {
				iter.remove();
				break;
			}
		}
	}

	public void popActivity(Activity activity) {
		removeActivity(activity);
	}

	public Activity getActivity(String className) {
		Iterator<WeakReference<Activity>> iter = activityStack.iterator();
		while (iter.hasNext()) {
			WeakReference<Activity> item = iter.next();

			Activity activity = item.get();
			if (activity == null) {
				iter.remove();
			} else {
				String curName = activity.getClass().getName();
				if (curName.compareTo(className) == 0)
					return activity;
			}
		}
		return null;
	}

	public void pushActivity(Activity activity) {
		activityStack.add(new WeakReference<Activity>(activity));
	}

	public void clear() {
		Iterator<WeakReference<Activity>> iter = activityStack.iterator();
		while (iter.hasNext()) {
			WeakReference<Activity> item = iter.next();
			Activity activity = item.get();
			if (activity != null) {
				activity.finish();
			}
		}

		activityStack.clear();
	}
	/**
	 * 通过类型结束栈中指定的Activity
	 * @param cls
	 */
	public void finishActivity(Class<?> cls) {
		for (WeakReference<Activity> weakReference : activityStack) {
			Activity activity = weakReference.get();
			if (activity.getClass().equals(cls)) {
				activity.finish();
				activityStack.remove(activity);
				break;
			}
		}
	}

	public void finishActivity(Activity activity) {
		if (activity != null) {
			activityStack.remove(activity);
			activity.finish();
			activity = null;
		}
	}
}
