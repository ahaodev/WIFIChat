package com.ahao.wifichat.ui.me;



import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ahao.wifichat.AppAction;
import com.ahao.wifichat.R;
import com.ahao.wifichat.net.Constants;
import com.ahao.wifichat.ui.BaseActivity;
import com.ahao.wifichat.ui.MainActivity;
import com.ahao.wifichat.utlis.NetworkUtils;
import com.ahao.wifichat.utlis.UiHelper;

public class LoginActivity extends BaseActivity {
	private EditText userEdit = null;
	private EditText nameEdit = null;
	private Button comfirmBtn = null;
	private String name;
	private String userName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 垂直显示
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.p2p_activity_login);
		userName = UiHelper.getShareData(this, AppAction.KEY.USER_NUMBER, AppAction.SHREAD.USER);
		name = UiHelper.getShareData(this, AppAction.KEY.USER_NAME, AppAction.SHREAD.USER);
		if (TextUtils.isEmpty(name) || TextUtils.isEmpty(userName)) {
			initView();
		} else {
			initView();
			userEdit.setText(userName);
			nameEdit.setText(name);
//			Intent intent = new Intent(this, MainActivity.class);
//			intent.putExtra(AppAction.KEY.USER_NUMBER, userName);
//			intent.putExtra(AppAction.KEY.USER_NAME, name);
//			startActivity(intent);
			app.getMe().setUsername(userName);
			app.getMe().setName(name);
			app.getMe().setIpAdderss(NetworkUtils.getLocalIpAddress());
			app.getMe().setPort(Constants.NETWORK_PRIVCHAT_PORT);

//			LoginActivity.this.finish();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initView() {
		userEdit = (EditText) findViewById(R.id.userid);// 用户名
		nameEdit = (EditText) findViewById(R.id.passwd);// 姓名
		comfirmBtn = (Button) findViewById(R.id.comfirm);
		comfirmBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				userName = userEdit.getText().toString();
				name = nameEdit.getText().toString();
				if (name == null || name.equals("")) {
					Toast.makeText(LoginActivity.this, getString(R.string.error_username_empty), Toast.LENGTH_SHORT)
							.show();
				} else if (name.length() < 1 || name.length() > 20) {
					Toast.makeText(LoginActivity.this, getString(R.string.error_username_length_invalid),
							Toast.LENGTH_SHORT).show();
				} else {
					if (name == null || name.equals("")) {
						Toast.makeText(LoginActivity.this, getString(R.string.error_password_empty), Toast.LENGTH_SHORT)
								.show();
					} else if (name.length() < 1 || name.length() > 32) {
						Toast.makeText(LoginActivity.this, getString(R.string.error_password_length_invalid),
								Toast.LENGTH_SHORT).show();

					} else {
						comfirmBtn.setEnabled(false);
						app.getMe().setUsername(userName);
						app.getMe().setName(name);
						app.getMe().setIpAdderss(NetworkUtils.getLocalIpAddress());
						app.getMe().setPort(Constants.NETWORK_PRIVCHAT_PORT);
						UiHelper.setShareData(LoginActivity.this, AppAction.SHREAD.USER, AppAction.KEY.USER_NUMBER ,
								userName);
						UiHelper.setShareData(LoginActivity.this, AppAction.SHREAD.USER, AppAction.KEY.USER_NAME,
								name);
						if (null!=app.getBinder()) {
							app.getBinder().getChatManager().sendEntry();
						}else {
							Toast.makeText(LoginActivity.this, "ServiceBinder暂未连接", Toast.LENGTH_SHORT).show();;
						}
						startActivity(MainActivity.class);

						LoginActivity.this.finish();
					}
				}
			}
		});
	}
}
