package com.ahao.wifichat.ui.me;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ahao.wifichat.R;
import com.ahao.wifichat.ui.BaseFragment;
import com.ahao.wifichat.ui.MainActivity;

public class PersonalCenterFragment extends BaseFragment {
	private View v;
	private TextView name;
	private TextView number;
	private MainActivity main;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.p2p_fragment_me, null);
		initView(v);

		return v;
	}
	@Override
	public void onResume() {
		super.onResume();
		main=(MainActivity) getActivity();
		main.getTitleView().setTitle(getTag());
		main.getTitleView().setRightVisibility(View.INVISIBLE);
	}
	private void initView(View v) {
		name = (TextView) v.findViewById(R.id.text_name);
		number = (TextView) v.findViewById(R.id.text_account);
		name.setText(app.getMe().getName());
		number.setText(app.getMe().getUsername());
		v.findViewById(R.id.btn_personal_infomation).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});
		v.findViewById(R.id.llsetting).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(SettingActivity.class);
			}
		});

	}
}
