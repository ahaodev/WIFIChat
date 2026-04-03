package com.ahao.wifichat.widget;

import java.util.ArrayList;

import com.ahao.wifichat.R;
import com.ahao.wifichat.ui.adapter.CommonAdapter;
import com.ahao.wifichat.ui.adapter.ViewHolder;
import com.ahao.wifichat.ui.chat.ChatActivity;
import com.ahao.wifichat.utlis.AudioRecoder;


import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 聊天底部文字语音输入视图
 * 
 * @author hao.ruan
 *
 */
public class ChatInputView extends LinearLayout {
	private ImageButton mBtnAddition;
	private LinearLayout mInputbody_layout;
	private Button mBtnSend;
	private TextView mInput;
	private Button btnSpeak;
	private ImageButton mBtnSwitch;
	private ArrayList<AdditionItem> items = new ArrayList<AdditionItem>();
	private GridView grid;
	private CommonAdapter<AdditionItem> adapter;
	private InputClickListener inputClickListener;
	private final AudioRecoder mAudioRecoder = new AudioRecoder();
	private int miScreenHeight = 0;
	private Context mContext;
	
	public ChatInputView(Context context) {
		super(context);
		initView(context);
	}

	public ChatInputView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public ChatInputView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	private void initView(Context context) {
		View.inflate(context, R.layout.p2p_view_chat_input, this);
		this.mContext=context;
		mHandler = new Handler();
		m_vibrator = (Vibrator) mContext.getApplicationContext().getSystemService(
				Service.VIBRATOR_SERVICE);
		mBtnAddition = (ImageButton) findViewById(R.id.btnAddition);
		mBtnAddition.setOnClickListener(mClickListener);

		mInputbody_layout = (LinearLayout) findViewById(R.id.inputbody_layout);

		mBtnSend = (Button) findViewById(R.id.btnSend);
		mBtnSend.setOnClickListener(mClickListener);

		mInput = (TextView) findViewById(R.id.TxtInput);
		mInput.setOnTouchListener(touchListener);

		btnSpeak = (Button) findViewById(R.id.btnSpeak);
		btnSpeak.setOnTouchListener(mInputTouchListener);

		mBtnSwitch = (ImageButton) findViewById(R.id.btnSwitch);
		mBtnSwitch.setOnClickListener(mClickListener);

		mInput.addTextChangedListener(textWatcher);

		/** ----- 隐藏的底部选项 ------ **/
		items.add(new AdditionItem(R.string.photo, R.drawable.icon_chat_add_photo));
		items.add(new AdditionItem(R.string.video, R.drawable.icon_chat_add_video));
		// new AdditionItem(R.string.voice, R.drawable.icon_chat_add_voice),
		items.add(new AdditionItem(R.string.location, R.drawable.icon_chat_add_location));
		// items.add(new AdditionItem(R.string.card,
		// R.drawable.icon_chat_add_card));
		items.add(new AdditionItem(R.string.File, R.drawable.icon_chat_add_file));

		grid = (GridView) findViewById(R.id.p2p_chat_input_grid);

		adapter = new CommonAdapter<AdditionItem>(context, items, R.layout.p2p_view_chat_input_textview) {

			@Override
			public void convert(ViewHolder helper, final AdditionItem item) {
				if (helper.getConvertView() instanceof TextView) {
					((TextView) helper.getConvertView()).setCompoundDrawablesWithIntrinsicBounds(null,
							getResources().getDrawable(item.resId), null, null);
					((TextView) helper.getConvertView()).setText(item.textId);
					((TextView) helper.getConvertView()).setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							switch (item.resId) {
							case R.drawable.icon_chat_add_photo:
								inputClickListener.sendImage();
								break;
							case R.drawable.icon_chat_add_video:
								inputClickListener.sendVideo();
								break;
							case R.drawable.icon_chat_add_location:
								inputClickListener.sendLocation();
								break;
							case R.drawable.icon_chat_add_file:
								inputClickListener.sendFile();
								break;
							default:
								break;
							}
						}
					});
				}
			}
		};
		grid.setAdapter(adapter);
		hideKeyBode();
	}

	private class AdditionItem {
		public final int textId;
		public final int resId;

		public AdditionItem(int t, int id) {
			textId = t;
			resId = id;
		}
	}

	private final OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.btnSwitch) {
				hideGroud();
				if (btnSpeak.getVisibility() != View.GONE) {
					mBtnSwitch.setImageResource(R.drawable.chat_input_voice);
					btnSpeak.setVisibility(View.GONE);
				} else {
					mBtnSwitch.setImageResource(R.drawable.chat_input_keyboard);
					btnSpeak.setVisibility(View.VISIBLE);
					mInput.setText("");
					hideKeyBode();
				}
			} else if (v.getId() == R.id.btnAddition) {
				hideKeyBode();
				if (grid.getVisibility() == View.GONE) {
					grid.setVisibility(View.VISIBLE);
					if (mContext instanceof ChatActivity) {
						ChatActivity chat =(ChatActivity) mContext;
						chat.smoothScroll(chat.adapter.getCount() - 1);
					}
				} else {
					grid.setVisibility(View.GONE);
				}

			} else if (v.getId() == R.id.btnSend) { // 发送文本
				String ret = mInput.getText().toString();
				mInput.setText("");
				inputClickListener.sendMessage(ret);
			}
		}

	};
	private final OnTouchListener touchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (v == mInput && event.getAction() == MotionEvent.ACTION_DOWN) {
				hideGroud();
			}
			return false;
		}
	};

	private boolean mReadyToCancelRecorder;
	private boolean mNeedStartRecorder;
	private Vibrator m_vibrator;

	public interface AudioRecordCallback {
		void onRecording(int iTime, int amplitude);

		void onStartRecord();

		void onReadyCancel();

		void onReturnNormal();
		
		void onStopRecord();
		/**
		 * 正常完成录音
		 * @param path
		 */
		void recodEnd(String path);
	}

	 public void setOnAudioRecordListener(AudioRecordCallback listener) {
		this.audioCall = listener;
	}

	private AudioRecordCallback audioCall;
	private final Runnable runnable = new Runnable() {

		@Override
		public void run() {
			if (mAudioRecoder == null)
				return;

			if (mNeedStartRecorder && !mAudioRecoder.isRun()) {
				// 震动
				m_vibrator.vibrate(30);

				if (audioCall != null)
					audioCall.onStartRecord();
				mAudioRecoder.startRecord(mContext.getApplicationContext());
			}
			if (!mNeedStartRecorder) {
				return;
			}

			int timedis = (int) (System.currentTimeMillis() - mAudioRecoder.getStartTime()) / 1000;

			if (audioCall != null)

				audioCall.onRecording(timedis, mAudioRecoder.getAmplitude());

			if (timedis < 60)
				mHandler.postDelayed(runnable, 100);
			else {
				sendVoice();
				btnSpeak.setSelected(false);
			}
		}
	};
	private final OnTouchListener mInputTouchListener = new OnTouchListener() {
		float downy = 0;

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			int action = arg1.getAction() & MotionEvent.ACTION_MASK;
			if (action == MotionEvent.ACTION_DOWN) {
				downy = arg1.getY();

				mNeedStartRecorder = true;
				mReadyToCancelRecorder = false;
				btnSpeak.setText(R.string.release_end);
				btnSpeak.setBackgroundResource(R.drawable.details_sendmsg_btn);
				mHandler.removeCallbacks(runnable);
				mHandler.postDelayed(runnable, 150);

			} else if (action == MotionEvent.ACTION_UP) {
				if (mReadyToCancelRecorder) {
					mNeedStartRecorder = false;
					btnSpeak.setText(R.string.down_speak);
					mHandler.removeCallbacks(runnable);
					mAudioRecoder.stopRecord();
					btnSpeak.setBackgroundResource(R.drawable.input_btn_normal);
					if (audioCall != null)
						audioCall.onStopRecord();
				} else {
					mNeedStartRecorder = false;
					sendVoice();
				}
			}
			if (action == MotionEvent.ACTION_MOVE) {

				int iMoveDis = miScreenHeight / 10;
				// 滑动取消录音
				if (downy - arg1.getY() >= iMoveDis) {

					if (!mReadyToCancelRecorder) {
						// 震动
						m_vibrator.vibrate(30);
						btnSpeak.setText(R.string.release_cancel);
						if (audioCall != null)
							audioCall.onReadyCancel();
					}
					mReadyToCancelRecorder = true;

				} else {
					if (mReadyToCancelRecorder) {
						if (audioCall != null)
							audioCall.onReturnNormal();
						btnSpeak.setText(R.string.release_end);
					}
					mReadyToCancelRecorder = false;
				}
			}
			return false;
		}
	};
	private Handler mHandler;

	private void sendVoice() {
		// 震动
		m_vibrator.vibrate(30);

		btnSpeak.setText(R.string.down_speak);
		btnSpeak.setBackgroundResource(R.drawable.input_btn_normal);
		mHandler.removeCallbacks(runnable);

		if (mAudioRecoder == null || !mAudioRecoder.isRun())
			return;

		mAudioRecoder.stopRecord();
		if (audioCall != null)
			audioCall.onStopRecord();
		if (audioCall != null && mAudioRecoder.getDuration() > 0) {
			String path = mAudioRecoder.getRecordPath();

			audioCall.recodEnd(path);
		}
		if (mAudioRecoder.getDuration() <= 0) {
			Toast.makeText(mContext, "录制时间太短", Toast.LENGTH_SHORT).show();
		}
	}

	private final TextWatcher textWatcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			if (mInput.getText().length() > 0) {
				mBtnAddition.setVisibility(View.GONE);
				mBtnSend.setVisibility(View.VISIBLE);
			} else {
				mBtnAddition.setVisibility(View.VISIBLE);
				mBtnSend.setVisibility(View.GONE);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

	};
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = getContext().getResources()
				.getDisplayMetrics();
		miScreenHeight = dm.heightPixels;
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	};
	
	/**
	 * 显示选项视图
	 */
	public void showGroud() {

	}

	/**
	 * 隐藏选项视图
	 */
	public void hideGroud() {
		grid.setVisibility(View.GONE);
	}

	public interface InputClickListener {
		void sendImage();

		void sendVideo();

		void sendLocation();

		void sendFile();

		void sendMessage(String message);

	}

	public void setOnInputClickListener(InputClickListener listener) {
		this.inputClickListener = listener;
	}

	/**
	 * 键盘隐藏
	 */
	public void hideKeyBode() {
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
	}

}
