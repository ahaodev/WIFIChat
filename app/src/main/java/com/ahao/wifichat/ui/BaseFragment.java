package com.ahao.wifichat.ui;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.ahao.wifichat.WiFiChat;

public class BaseFragment extends Fragment {
	protected WiFiChat app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 app= WiFiChat.instance();
		Log.e("ClassLoder", this.getClass().getPackage().toString() + getClass().getSimpleName());
	}

	public void startActivity(Class<?> cls) {
		Intent intent = new Intent(getActivity(), cls);
		getActivity().startActivity(intent);
	}

	public void startActivity(Class<?> cls, String key, Bundle bundle) {
		Intent intent = new Intent(getActivity(), cls);
		intent.putExtra(key, bundle);
		getActivity().startActivity(intent);
	}

	public void startActivity(Class<?> cls, String key, Parcelable parcelable) {
		Intent intent = new Intent(getActivity(), cls);
		intent.putExtra(key, parcelable);
		getActivity().startActivity(intent);
	}
}
