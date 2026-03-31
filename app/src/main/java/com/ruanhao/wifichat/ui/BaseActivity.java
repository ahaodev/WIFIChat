package com.ruanhao.wifichat.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ruanhao.wifichat.WiFiChat;


public class BaseActivity extends Activity {
	protected WiFiChat app = WiFiChat.instance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置无标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 垂直显示
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		app.getActivityManager().pushActivity(this);
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		applySystemBarInsets();
	}

	/**
	 * 为根布局应用系统栏内边距，防止内容被状态栏和导航栏遮挡
	 */
	protected void applySystemBarInsets() {
		ViewGroup contentParent = findViewById(android.R.id.content);
		if (contentParent != null && contentParent.getChildCount() > 0) {
			View rootView = contentParent.getChildAt(0);
			final int pl = rootView.getPaddingLeft();
			final int pt = rootView.getPaddingTop();
			final int pr = rootView.getPaddingRight();
			final int pb = rootView.getPaddingBottom();
			ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
				Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
				v.setPadding(pl + insets.left, pt + insets.top, pr + insets.right, pb + insets.bottom);
				return WindowInsetsCompat.CONSUMED;
			});
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		app.getActivityManager().popActivity(this);
	}

	public void startActivity(Class<?> cls) {
		Intent intent = new Intent(this, cls);
		startActivity(intent);
	}

	public void startActivity(Class<?> cls, String key, Bundle bundle) {
		Intent intent = new Intent(this, cls);
		intent.putExtra(key, bundle);
		startActivity(intent);
	}

	public void startActivity(Class<?> cls, String key, Parcelable parcelable) {
		Intent intent = new Intent(this, cls);
		intent.putExtra(key, parcelable);
		startActivity(intent);
	}
}
