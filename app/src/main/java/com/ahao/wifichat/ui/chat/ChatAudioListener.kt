package com.ahao.wifichat.ui.chat;



import android.view.View;
import android.widget.TextView;

import com.ahao.wifichat.R;
import com.ahao.wifichat.widget.ChatInputView;

public class ChatAudioListener implements ChatInputView.AudioRecordCallback {
	ChatActivity chat;

	public ChatAudioListener(ChatActivity chat) {
		this.chat = chat;
	}

	@Override
	public void onRecording(int iTime, int amplitude) {

		final int vols[] = { R.drawable.voice_record_1, R.drawable.voice_record_2, R.drawable.voice_record_3,
				R.drawable.voice_record_4, R.drawable.voice_record_5, R.drawable.voice_record_6,
				R.drawable.voice_record_7, R.drawable.voice_record_8 };
		// 将音频转成分贝
		int ratio = amplitude / 600;
		int db = 0;// 分贝
		if (ratio > 1)
			db = (int) (20 * Math.log10(ratio));
		// 分级
		db = db / 4;
		if (db > 7)
			db = 7;

		((TextView) chat.findViewById(R.id.voice_volume)).setBackgroundResource(vols[db]);

		if (iTime >= 50) {
			String format = "时间过短";
			String str = String.format(format, 60 - iTime);
			((TextView) chat.findViewById(R.id.tv_voice_time_count_down)).setText(str);
		}

	}

	@Override
	public void onStartRecord() {
		((TextView) chat.findViewById(R.id.voice_volume)).setBackgroundResource(R.drawable.voice_record_3);
		chat.findViewById(R.id.recordimage).setVisibility(View.VISIBLE);

		((TextView) chat.findViewById(R.id.tv_voice_time_count_down)).setText(R.string.cancel_sendvoice);
		onReturnNormal();
	}

	@Override
	public void onStopRecord() {
		chat.findViewById(R.id.recordimage).setVisibility(View.INVISIBLE);
		chat.findViewById(R.id.recordimage_cancel).setVisibility(View.INVISIBLE);
	}

	@Override
	public void onReadyCancel() {
		chat.findViewById(R.id.recordimage_cancel).setVisibility(View.VISIBLE);

		chat.findViewById(R.id.recordimage).setVisibility(View.INVISIBLE);
	}

	@Override
	public void onReturnNormal() {
		chat.findViewById(R.id.recordimage).setVisibility(View.VISIBLE);

		chat.findViewById(R.id.recordimage_cancel).setVisibility(View.INVISIBLE);
	}

	@Override
	public void recodEnd(String path) {
		chat.sendAudio(path);
	}

}
