package com.ahao.wifichat.ui.chat;

import java.util.ArrayList;
import java.util.List;

import com.ahao.wifichat.R;
import com.ahao.wifichat.db.DBConstants;
import com.ahao.wifichat.db.msg;
import com.ahao.wifichat.db.msg_attachment;
import com.ahao.wifichat.db.user_msg;
import com.ahao.wifichat.utlis.StringUtils;
import com.ahao.wifichat.widget.ChatMessageView;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ChatAdapter extends BaseAdapter implements OnClickListener {
	// 定义当前listview是否在滑动状态
	private boolean scrollState = false;

	protected List<user_msg> mDatas = new ArrayList<user_msg>();
	private Context context;

	public ChatAdapter(Context context, List<user_msg> list) {
		this.context = context;
		this.mDatas = list;
	}

	@Override
	public int getCount() {
		return mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return mDatas.get(position).getDir_type();
	}

	public void notifyDataSetChanged(List<user_msg> datas) {
		this.mDatas = datas;
		notifyDataSetChanged();
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final user_msg user_msg = (user_msg) getItem(position);
		msg msg=user_msg.getMessageTab();
		msg_attachment attch=msg.queryMsg_att();
		View v = null;
		int messageType = getItemViewType(position);
		LayoutInflater inflater = LayoutInflater.from(context);
		ChatMessageView cmv;
		String timeTxt = StringUtils.formatTime(msg.getMsg_time());
		switch (messageType) {
		case DBConstants.DIR_TYPE_I:
			v = inflater.inflate(R.layout.p2p_list_item_wechat_out, null);
			TextView timeView = (TextView) v.findViewById(R.id.tvtime);
			ProgressBar pbMsg = (ProgressBar) v.findViewById(R.id.pbMsg);
			if (user_msg.getStatus() == DBConstants.SEND_STATUS_ING) {

			} else if (user_msg.getStatus() == DBConstants.SEND_STATUS_OK) {
				pbMsg.setVisibility(View.GONE);
			} else if (user_msg.getStatus() == DBConstants.SEND_STATUS_ERR) {
				ImageView ivRetry = (ImageView) v.findViewById(R.id.ivRetry);
				ivRetry.setVisibility(View.VISIBLE);
				pbMsg.setVisibility(View.GONE);
			}

			timeView.setText(timeTxt);
			cmv = (ChatMessageView) v.findViewById(R.id.tvcontent);
			cmv.setMessage(user_msg,msg,attch,scrollState);
			cmv.setOnClickListener(this);
			break;
		case DBConstants.DIR_TYPE_Y:
			v = inflater.inflate(R.layout.p2p_list_item_wechat_in, null);
			TextView timeView2 = (TextView) v.findViewById(R.id.tvtime);
			timeView2.setText(timeTxt);
			cmv = (ChatMessageView) v.findViewById(R.id.tvcontent);
			cmv.setMessage(user_msg,msg,attch,scrollState);
			cmv.setOnClickListener(this);
			cmv.setTag(user_msg);
			break;
		default:
			break;
		}
		return v;
	}

	public void setScrollState(boolean scrollState) {
		this.scrollState = scrollState;
	}

	@Override
	public void onClick(View v) {
		user_msg user_msg;
		msg msg;
		msg_attachment attch;
		Intent intent;
		if (v instanceof ChatMessageView) {
			user_msg = ((ChatMessageView) v).getMessage();
			msg=user_msg.getMessageTab();
			attch = msg.queryMsg_att();
			switch (user_msg.getMessageTab().getType()) {
			case DBConstants.MSG_TYPE_TXT:

				break;
			case DBConstants.MSG_TYPE_LOCATION:
	/*			MsgLocation location = new Gson().fromJson(user_msg.getMessageTab().getContent(), MsgLocation.class);
				intent = new Intent(context, MapLookActivity.class);
				intent.putExtra(MapLookActivity.ADDR, location.getName());
				intent.putExtra(MapLookActivity.LON, location.getLon());
				intent.putExtra(MapLookActivity.LAT, location.getLat());
				context.startActivity(intent);*/
				break;
			case DBConstants.MSG_TYPE_FILE:
				switch (attch.getType()) {
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
	}

}
