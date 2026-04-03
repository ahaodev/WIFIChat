package com.ahao.wifichat.ui.chat;

import java.io.File;
import java.util.List;

import com.google.gson.Gson;
import com.ahao.wifichat.R;
import com.ahao.wifichat.WifiChatApplication;
import com.ahao.wifichat.db.DBConstants;
import com.ahao.wifichat.db.msg_attachment;
import com.ahao.wifichat.db.user_msg;
import com.ahao.wifichat.protocol.v1.MsgAttachment;
import com.ahao.wifichat.service.parcelable.OnlineUserInfo;
import com.ahao.wifichat.ui.adapter.CommonAdapter;
import com.ahao.wifichat.ui.adapter.ViewHolder;
import com.ahao.wifichat.utlis.AudioPlayer;
import com.ahao.wifichat.utlis.GlideLoder;
import com.ahao.wifichat.utlis.ImageLoader;
import com.ahao.wifichat.utlis.StringUtils;


import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

/**
 * 聊天各种消息的适配器
 * 
 * @author hao.ruan
 *
 */
public class ChatMessageAdapter extends CommonAdapter<user_msg> {
	private final ImageLoader imageLoader;
	private OnlineUserInfo user;

	public ChatMessageAdapter(Context context, List<user_msg> mDatas, int itemLayoutId) {
		super(context, mDatas, itemLayoutId);
		 imageLoader=new GlideLoder(context);
	}

	@Override
	protected ViewHolder getViewHolder(int position, View convertView, ViewGroup parent) {
		boolean isme = isMe(position);
		switch (getItemViewType(position)) {
		case DBConstants.MSG_TYPE_TXT:
			if (isme) {
				return ViewHolder.get(mContext, convertView, parent, R.layout.item_chat_text_r, position);
			} else {
				return ViewHolder.get(mContext, convertView, parent, R.layout.item_chat_text_l, position);
			}
		case DBConstants.MSG_TYPE_LOCATION:
			if (isme) {
				return ViewHolder.get(mContext, convertView, parent, R.layout.item_chat_location_r, position);
			} else {
				return ViewHolder.get(mContext, convertView, parent, R.layout.item_chat_location_l, position);
			}
		case DBConstants.MSG_TYPE_FILE:
			switch (mDatas.get(position).getMessageTab().queryMsg_att().getType()) {
			case DBConstants.FILE_TYPE:

				if (isme) {
					return ViewHolder.get(mContext, convertView, parent, R.layout.item_chat_fiel_r, position);
				} else {
					return ViewHolder.get(mContext, convertView, parent, R.layout.item_chat_fiel_l, position);
				}
			case DBConstants.FILE_TYPE_IMG:
				if (isme) {
					return ViewHolder.get(mContext, convertView, parent, R.layout.item_chat_image_r, position);
				} else {
					return ViewHolder.get(mContext, convertView, parent, R.layout.item_chat_image_l, position);
				}
			case DBConstants.FILE_TYPE_AUDIO:
				if (isme) {
					return ViewHolder.get(mContext, convertView, parent, R.layout.item_chat_audio_r, position);
				} else {
					return ViewHolder.get(mContext, convertView, parent, R.layout.item_chat_audio_l, position);
				}
			case DBConstants.FILE_TYPE_VIDEO:
				if (isme) {
					return ViewHolder.get(mContext, convertView, parent, R.layout.item_chat_video_r, position);
				} else {
					return ViewHolder.get(mContext, convertView, parent, R.layout.item_chat_video_l, position);
				}
			default:
				break;
			}
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public int getItemViewType(int position) {
		return mDatas.get(position).getMessageTab().getType();
	}

	@Override
	public int getViewTypeCount() {
		return 15;// 大于消息类型x2的数量
	}

	public void setUser(OnlineUserInfo user) {
		this.user = user;
	}

	@Override
	public void convert(final ViewHolder helper, final user_msg item) {
		String timeTxt = StringUtils.formatTime(item.getMessageTab().getMsg_time());
		helper.setText(R.id.tvtime, timeTxt);
		if (item.getDir_type() == DBConstants.DIR_TYPE_I) {

			ProgressBar pbMsg = helper.getView(R.id.pbMsg);
			if (item.getStatus() == DBConstants.SEND_STATUS_ING) {

			} else if (item.getStatus() == DBConstants.SEND_STATUS_OK) {
				pbMsg.setVisibility(View.GONE);
			} else if (item.getStatus() == DBConstants.SEND_STATUS_ERR) {
				helper.setVisibility(R.id.ivRetry, View.VISIBLE);
				pbMsg.setVisibility(View.GONE);
			}
		} else {

		}
		switch (item.getMessageTab().getType()) {
		case DBConstants.MSG_TYPE_TXT:
			helper.setText(R.id.tvContent, item.getMessageTab().getContent());
			break;
		case DBConstants.MSG_TYPE_LOCATION:

			break;
		case DBConstants.MSG_TYPE_FILE:
			msg_attachment attch = item.getMessageTab().queryMsg_att();
			final String filePath;// 文件路径
			if (item.getDir_type() == DBConstants.DIR_TYPE_I) {
				filePath = attch.getUri();
			} else {
				String fileName = attch.getUri().substring(attch.getFilename().lastIndexOf("/") + 1);
				filePath = WifiChatApplication.getInstance().getAppPath() + File.separator + item.getOther_username()
						+ File.separator + fileName;
			}
			switch (item.getMessageTab().queryMsg_att().getType()) {
			case DBConstants.FILE_TYPE:

				break;
			case DBConstants.FILE_TYPE_IMG:
				helper.setImageByUrl(R.id.ivPhoto,"file://" + filePath);
				break;
			case DBConstants.FILE_TYPE_AUDIO:
				if (item.getDir_type() == DBConstants.DIR_TYPE_I) {
				} else {
				}
				break;
			case DBConstants.FILE_TYPE_VIDEO:
				if (item.getDir_type() == DBConstants.DIR_TYPE_I) {
				} else {
				}
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}

	}

	public OnItemClickListener itemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Toast.makeText(mContext, getItem(position).getMessageTab().getContent(), Toast.LENGTH_SHORT).show();
			user_msg message = getItem(position);
			switch (message.getMessageTab().getType()) {
			case DBConstants.MSG_TYPE_TXT:

				break;
			case DBConstants.MSG_TYPE_LOCATION:
/*				MsgLocation location = new Gson().fromJson(message.getMessageTab().getContent(), MsgLocation.class);
				Intent intent = new Intent(mContext, MapLookActivity.class);
				intent.putExtra(MapLookActivity.ADDR, location.getName());
				intent.putExtra(MapLookActivity.LON, location.getLon());
				intent.putExtra(MapLookActivity.LAT, location.getLat());
				mContext.startActivity(intent);*/
				break;
			case DBConstants.MSG_TYPE_FILE:
				MsgAttachment attach = new Gson().fromJson(message.getMessageTab().getContent(), MsgAttachment.class);
				switch (attach.getType()) {
				case DBConstants.FILE_TYPE_IMG:

					break;
				case DBConstants.FILE_TYPE_AUDIO:

					break;
				case DBConstants.FILE_TYPE_VIDEO:

					break;
				case DBConstants.FILE_TYPE:

					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
		}
	};

	private boolean isMe(int position) {
		if (mDatas.get(position).getDir_type() == DBConstants.DIR_TYPE_I) {
			return true;
		}
		return false;

	}

	/**
	 * 跟新播放语音状态
	 * 
	 * @param tv
	 * @param isplay
	 */
	public void updataVoiceIcon(user_msg message,TextView tv, boolean isplay) {
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

		final Drawable drawable = mContext.getResources().getDrawable(drawId);
		if (message.getDir_type() == DBConstants.DIR_TYPE_I) {
			tv.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
		} else {
			tv.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
		}

		if (drawId == playId) {
			tv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
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
	public void updateVoiceView(user_msg message,msg_attachment attch,boolean isplay) {
		if (message == null || attch.getType() != DBConstants.FILE_TYPE_AUDIO)
			return;

		//TextView tv = (TextView) findViewById(R.id.tvVoice);
		//updataVoiceIcon(message ,tv, isplay);
	}

	private MediaPlayer mPlayFinishedMedia;
	private AudioPlayer.onPlayListener playListener = new AudioPlayer.onPlayListener() {

		@Override
		public void onStop(String path) {
			if (TextUtils.isEmpty(path))
				return;
		//	updateVoiceView(false);
		}
	};
	private AudioPlayer mAudioPlayer = new AudioPlayer(playListener);

}
