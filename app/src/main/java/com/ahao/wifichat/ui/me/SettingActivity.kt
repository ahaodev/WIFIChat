package com.ahao.wifichat.ui.me;



import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.ahao.wifichat.R;
import com.ahao.wifichat.ui.BaseActivity;
import com.ahao.wifichat.widget.TitleView;

public class SettingActivity extends BaseActivity {
	private TitleView titleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.p2p_activity_setting);
		titleView = (TitleView) findViewById(R.id.title);
		titleView.setTitle("设置");
		findViewById(R.id.btn_logout).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				app.getBinder().getChatManager().sendOutLine();

				app.getMe().setName("");

				app.getMe().setPort(0);

				app.getMe().setIpAdderss("");

				app.getMe().setUsername("");

//				UiHelper.setShareData(SettingActivity.this, AppAction.SHREAD.USER, AppAction.KEY.USER_NAME, "");
//				UiHelper.setShareData(SettingActivity.this, AppAction.SHREAD.USER, AppAction.KEY.USER_NUMBER, "");
				startActivity(LoginActivity.class);
				SettingActivity.this.finish();
				app.getBinder().OnlineUsersClear();//清空在线人员以及在线人员位置
				app.getActivityManager().clear();//销毁Activity栈
			}
		});
		findViewById(R.id.btn_about_wechat).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(AboutActivity.class);
			}
		});
	}
}
