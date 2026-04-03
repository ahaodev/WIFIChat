package com.ahao.wifichat.widget;

import java.io.File;



import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ahao.wifichat.R;
import com.ahao.wifichat.WifiChatApplication;
import com.ahao.wifichat.db.DBConstants;
import com.ahao.wifichat.db.msg;
import com.ahao.wifichat.db.msg_attachment;
import com.ahao.wifichat.db.user_msg;
import com.ahao.wifichat.ui.chat.image.ChatImagesActivity;
import com.ahao.wifichat.ui.chat.video.WeixinVideoPlayerActivity;
import com.ahao.wifichat.utlis.AudioPlayer;
import com.ahao.wifichat.utlis.GlideLoder;
import com.ahao.wifichat.utlis.ImageUtils;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ChatMessageView extends FrameLayout {
	private user_msg message;
	private msg msg;
	private Context context;
	private msg_attachment attch;
	public GlideLoder imageLoader;
	public ChatMessageView(Context context) {
		super(context);
		imageLoader=new GlideLoder(context);
	}

	public ChatMessageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public ChatMessageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public user_msg getMessage() {
		return message;
	}

	public void setMessage(final user_msg message,msg msg,msg_attachment attach,final boolean scrollState) {
		if (imageLoader==null){
			imageLoader=new GlideLoder(context);
		}
		this.message = message;
		this.msg=msg;
		this.attch=attach;
		LayoutInflater inflater = LayoutInflater.from(getContext());
		switch (msg.getType()) {
		case DBConstants.MSG_TYPE_TXT: {
			View v = inflater.inflate(R.layout.watch_com_chat_msgview, this);
			TextView tv = (TextView) v.findViewById(R.id.tvContent);
			String txt = msg.getContent();
			tv.setText(txt);
		}
			break;
		case DBConstants.MSG_TYPE_LOCATION: {
			inflater.inflate(R.layout.com_chat_msg_location, this);
		}

			break;
		case DBConstants.MSG_TYPE_FILE: {
			final String filePath;// 文件路径
			if (message.getDir_type() == 1) {// 自己的直接是路径
				//filePath = attch.getName();
				filePath = attch.getUri();
			} else {// 发过来的根据对方username找文件
				String fileName = attch.getUri().substring(attch.getFilename().lastIndexOf("/") + 1);
				filePath = WifiChatApplication.getInstance().getAppPath() + File.separator + message.getOther_username()
						+ File.separator + fileName;
			}

			switch (attch.getType()) {
			case DBConstants.FILE_TYPE_IMG: {
				View v = inflater.inflate(R.layout.com_chat_msg_photo, this);
				final ImageView iv = (ImageView) v.findViewById(R.id.ivPhoto);

				final Intent intent = new Intent(context, ChatImagesActivity.class);
				intent.putExtra("url", filePath);
//				imageLoader.loadImageByFile(iv, filePath);
				File mFile=new File(filePath);
				Glide
						.with(context)
						.load(mFile)
						.placeholder(R.drawable.atach_image)
						.override(180, 240) // resizes the image to these dimensions (in pixel)
						.centerCrop() // this cropping technique scales the image so that it fills the requested bounds and then crops the extra.
						.error(R.drawable.logout_btn)
						.into(iv);

				iv.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						context.startActivity(intent);
					}
				});


			}
				break;
			case DBConstants.FILE_TYPE_AUDIO: {
				View v = inflater.inflate(R.layout.com_chat_msg_voice, this);
				TextView tv = (TextView) v.findViewById(R.id.tvVoice);
				updataVoiceIcon(tv, false);
				tv.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mAudioPlayer.startPlay(filePath);
						updateVoiceView(true);
					}
				});
			}
				break;
			case DBConstants.FILE_TYPE_VIDEO: {
				View v = inflater.inflate(R.layout.com_chat_msg_video, this);
				TextView tv = (TextView) v.findViewById(R.id.tvVideo);
				final ImageView iv = (ImageView) v.findViewById(R.id.ivPreview);

				Observable.just(filePath).subscribeOn(Schedulers.io()).map(new Func1<String, Bitmap>() {

					@Override
					public Bitmap call(String arg0) {
						return ImageUtils.getVideoBitmap(filePath, 240, 240);
					}
				}).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Bitmap>() {

					@Override
					public void call(Bitmap bitmap) {
						iv.setImageBitmap(bitmap);
					}
				});
				iv.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(context, WeixinVideoPlayerActivity.class);
						intent.putExtra("url", filePath);
						context.startActivity(intent);
					}
				});
			}

				break;
			case DBConstants.FILE_TYPE:
				View v = inflater.inflate(R.layout.com_chat_msg_file, this);
				TextView tvFile = (TextView) v.findViewById(R.id.tvFile);
				//tvFile.setText(_msg.getFileName());
				break;
			default:
				break;
			}
		}
			break;
		default:
			break;
		}
	}

	/**
	 * 跟新播放语音状态
	 * 
	 * @param tv
	 * @param isplay
	 */
	public void updataVoiceIcon(TextView tv, boolean isplay) {
		int playId = R.drawable.animation_voice_play_right;
		int voiceId = R.drawable.voice_play_right_3;

		if (message.getDir_type() == DBConstants.DIR_TYPE_Y) {
			playId = R.drawable.animation_voice_play_left;
			voiceId = R.drawable.voice_play_left_3;
		}

		int drawId = voiceId;
		if (isplay) {
			drawId = playId;
		}

		final Drawable drawable = getResources().getDrawable(drawId);
		if (message.getDir_type() == DBConstants.DIR_TYPE_I) {
			tv.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
		} else {
			tv.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
		}

		if (drawId == playId) {
			getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					AnimationDrawable animation = (AnimationDrawable) drawable;
					animation.start();
					return true;
				}
			});
		}
	}

	/**
	 * 播放语音开关
	 * 
	 * @param isplay
	 */
	public void updateVoiceView(boolean isplay) {
		if (message == null || attch.getType() != DBConstants.FILE_TYPE_AUDIO)
			return;

		TextView tv = (TextView) findViewById(R.id.tvVoice);
		updataVoiceIcon(tv, isplay);
	}

	private MediaPlayer mPlayFinishedMedia;
	private AudioPlayer.onPlayListener playListener = new AudioPlayer.onPlayListener() {

		@Override
		public void onStop(String path) {
			if (TextUtils.isEmpty(path))
				return;
			updateVoiceView(false);
		}
	};
	private AudioPlayer mAudioPlayer = new AudioPlayer(playListener);

	private void playVoice(String path) {
		if (!mAudioPlayer.isRun())
			mAudioPlayer.startPlay(path);
	}
}
