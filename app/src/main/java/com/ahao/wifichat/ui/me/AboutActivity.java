package com.ahao.wifichat.ui.me;



import android.os.Bundle;

import com.ahao.wifichat.R;
import com.ahao.wifichat.ui.BaseActivity;
import com.ahao.wifichat.widget.TitleView;

public class AboutActivity extends BaseActivity {
	private TitleView titleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		titleView = (TitleView) findViewById(R.id.title);
		titleView.setTitle("关于");
	}
}
