package com.ahao.wifichat.service;

import com.google.gson.Gson;
import com.ahao.wifichat.WiFiChat;
import com.ahao.wifichat.db.ChatDB;
import com.ahao.wifichat.db.DBConstants;
import com.ahao.wifichat.entity.Userinfo;
import com.ahao.wifichat.net.Constants;
import com.ahao.wifichat.net.DataConnection;
import com.ahao.wifichat.net.file.FileState;
import com.ahao.wifichat.protocol.P2pCmd;
import com.ahao.wifichat.protocol.codec.Pack;
import com.ahao.wifichat.protocol.constant.EnumMsgType;
import com.ahao.wifichat.protocol.v1.BRLoaction;
import com.ahao.wifichat.protocol.v1.Entry;
import com.ahao.wifichat.protocol.v1.MessageInfo;
import com.ahao.wifichat.protocol.v1.MsgAttachment;
import com.ahao.wifichat.protocol.v1.MsgAttachmentContent;
import com.ahao.wifichat.protocol.v1.MsgLocation;
import com.ahao.wifichat.protocol.v1.MsgText;
import com.ahao.wifichat.service.filter.PacketReceiveFilter;
import com.ahao.wifichat.service.parcelable.OnlineUserInfo;
import com.ahao.wifichat.service.parcelable.ReplyMessage;
import com.ahao.wifichat.service.parcelable.UserLocation;
import com.ahao.wifichat.service.parcelable.UserMessage;
import com.ahao.wifichat.utlis.GsonUtils;


import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * 解析协议
 * 
 * @author xiang.shen
 *
 */
public class ProcessReceiverData {

	private final DataConnection mDataConnection;
	private ProcessListener mProcessListener;
	private ProcessListener.LocationListener locationListener;
	private final P2PService mService;
	private final Userinfo mSelfUserInfo;

	private final Handler mHandle = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case P2pCmd.CMD_BR_ENTRY: {
				if (null == msg.obj || !(msg.obj instanceof OnlineUserInfo))
					break;

				OnlineUserInfo userInfo = (OnlineUserInfo) msg.obj;

				mService.addOnlineUser(userInfo);

				if (mProcessListener != null)
					mProcessListener.onOnlineUserUpdate(userInfo);
				break;
			}
			case P2pCmd.CMD_ANS_ENTRY: {
				if (null == msg.obj || !(msg.obj instanceof OnlineUserInfo))
					break;

				OnlineUserInfo userInfo = (OnlineUserInfo) msg.obj;

				mService.addOnlineUser(userInfo);
				if (mProcessListener != null)
					mProcessListener.onOnlineUserUpdate(userInfo);
				break;
			}
			case P2pCmd.CMD_BR_EXIT: {
				if (null == msg.obj || !(msg.obj instanceof OnlineUserInfo))
					break;

				OnlineUserInfo userInfo = (OnlineUserInfo) msg.obj;

				mService.delOnlineUser(userInfo.getUserName());
				if (mProcessListener != null)
					mProcessListener.onOnlineUserUpdate(userInfo);

				break;
			}
			case P2pCmd.CMD_SEND_TEXT: {
				if (null == msg.obj || !(msg.obj instanceof UserMessage))
					break;

				UserMessage usermsg = (UserMessage) msg.obj;

				if (mProcessListener != null)
					mProcessListener.onNewMessage(usermsg);

				runVibrator();
				break;
			}
			case P2pCmd.CMD_SEND_LOC: {
				if (null == msg.obj || !(msg.obj instanceof UserMessage))
					break;

				UserMessage usermsg = (UserMessage) msg.obj;

				if (mProcessListener != null)
					mProcessListener.onNewMessage(usermsg);

				runVibrator();
				break;
			}
			case P2pCmd.CMD_FILE_NOTICE: {
				if (null == msg.obj || !(msg.obj instanceof UserMessage))
					break;
				UserMessage usermsg = (UserMessage) msg.obj;
				if (mProcessListener != null)
					mProcessListener.onNewMessage(usermsg);

				runVibrator();
				break;
			}
			case P2pCmd.CMD_BR_LOC: {
				if (null == msg.obj || !(msg.obj instanceof UserLocation))
					break;
				UserLocation message = (UserLocation) msg.obj;
				if (locationListener != null)
					locationListener.onLocationMessage(message);
				mService.addOnlineUserLocation(message);
				break;
			}
			}
		}
	};

	// 监听上线通知（广播）
	private PacketListener processEntryListener = new PacketListener() {

		@Override
		public void processPacket(Pack packet) {
			if (TextUtils.isEmpty(packet.getContent()))
				return;
			Entry entry = GsonUtils.getJson(packet.getContent(), Entry.class);
			if (null == entry)
				return;
			OnlineUserInfo userInfo = new OnlineUserInfo();
			userInfo.setUserName(packet.getSendName());
			userInfo.setName(entry.getName());
			userInfo.setIpAdderss(entry.getIpAdderss());
			userInfo.setPort(Constants.NETWORK_UDP_PORT);

			sendMessage(P2pCmd.CMD_BR_ENTRY, userInfo);

			Pack pack = PackCommand.getInstance().packReplyEntry(mSelfUserInfo.getUsername(), mSelfUserInfo.getName(),
					mSelfUserInfo.getIpAdderss());
			mDataConnection.sendData(pack);
		}
	};

	// 监听上线通知回复
	private PacketListener processAnswerEntryListener = new PacketListener() {

		@Override
		public void processPacket(Pack packet) {
			if (TextUtils.isEmpty(packet.getContent()))
				return;
			Entry entry = GsonUtils.getJson(packet.getContent(), Entry.class);
			if (null == entry)
				return;
			OnlineUserInfo userInfo = new OnlineUserInfo();
			userInfo.setUserName(packet.getSendName());
			userInfo.setName(entry.getName());
			userInfo.setIpAdderss(entry.getIpAdderss());
			userInfo.setPort(Constants.NETWORK_UDP_PORT);

			sendMessage(P2pCmd.CMD_ANS_ENTRY, userInfo);
		}
	};

	// 监听下线通知
	private PacketListener processExitListener = new PacketListener() {

		@Override
		public void processPacket(Pack packet) {

			OnlineUserInfo userInfo = new OnlineUserInfo();
			userInfo.setUserName(packet.getSendName());
			sendMessage(P2pCmd.CMD_BR_EXIT, userInfo);
		}
	};

	// 监听文本消息
	private PacketListener processTextListener = new PacketListener() {

		@Override
		public void processPacket(Pack packet) {
			if (TextUtils.isEmpty(packet.getContent()))
				return;

			MessageInfo<MsgText> data = GsonUtils.fromJsonObject(packet.getContent(), MsgText.class);
			if (null == data || null == data.getContent() || !(data.getContent() instanceof MsgText))
				return;

			long time = System.currentTimeMillis();
			OnlineUserInfo user = mService.getOnlineUser(packet.getSendName());

			MsgText text = data.getContent();

			ChatDB.getInstance().insertMessage(DBConstants.DIR_TYPE_Y, text.getText(), DBConstants.MSG_TYPE_TXT, time,
					WiFiChat.instance().getMe().getUsername(), user);

			UserMessage msg = new UserMessage();
			msg.setType(EnumMsgType.TXT);
			msg.setContent(text.toJson());
			msg.setMsg_time(time);
			msg.setSendUsername(packet.getSendName());

			sendMessage(P2pCmd.CMD_SEND_TEXT, msg);

			if (null != user) {
				Pack pack = PackCommand.getInstance().packReplyMessage(mSelfUserInfo.getUsername(), packet.getCommand(),
						packet.getSn(), data.getMsg_id());
				mDataConnection.sendData(user, pack);
			}
		}
	};

	// 监听位置消息
	private PacketListener processLocationListener = new PacketListener() {

		@Override
		public void processPacket(Pack packet) {
			if (TextUtils.isEmpty(packet.getContent()))
				return;

			MessageInfo<MsgLocation> data = GsonUtils.fromJsonObject(packet.getContent(), MsgLocation.class);
			if (null == data || null == data.getContent() || !(data.getContent() instanceof MsgLocation))
				return;
			OnlineUserInfo user = mService.getOnlineUser(packet.getSendName());
			MsgLocation loc = data.getContent();

			UserMessage msg = new UserMessage();
			msg.setType(EnumMsgType.POS);
			msg.setContent(loc.toJson());
			msg.setMsg_time(System.currentTimeMillis());
			msg.setSendUsername(packet.getSendName());

			ChatDB.getInstance().insertMessage(DBConstants.DIR_TYPE_Y, loc.toJson(), DBConstants.MSG_TYPE_LOCATION,
					System.currentTimeMillis(), WiFiChat.instance().getMe().getUsername(), user);
			sendMessage(P2pCmd.CMD_SEND_LOC, msg);

			if (null != user) {
				Pack pack = PackCommand.getInstance().packReplyMessage(mSelfUserInfo.getUsername(), packet.getCommand(),
						packet.getSn(), data.getMsg_id());
				mDataConnection.sendData(user, pack);
			}
		}
	};

	// 监听附件消息
	private PacketListener processAttachmentListener = new PacketListener() {

		@Override
		public void processPacket(Pack packet) {
			if (TextUtils.isEmpty(packet.getContent()))
				return;

			// MessageInfo<List<MsgAttachment>> data =
			// GsonUtils.fromJsonArray(packet.getContent(),
			// MsgAttachment.class);
			// if (null == data || null == data.getContent())
			// return;

			MsgAttachmentContent data = new Gson().fromJson(packet.getContent(), MsgAttachmentContent.class);
			MsgAttachment msgAttachment = data.getAttachment();

			UserMessage msg = new UserMessage();
			msg.setType(EnumMsgType.ATTACHMENT);
			msg.setContent(packet.getContent());
			msg.setMsg_time(System.currentTimeMillis());
			msg.setSendUsername(packet.getSendName());

			OnlineUserInfo user = mService.getOnlineUser(packet.getSendName());
			FileState filestate = new FileState(msgAttachment.getSize(), 1, msgAttachment.getName(),
					msgAttachment.getType());

			ChatDB.getInstance().insertMessage(DBConstants.DIR_TYPE_Y, msgAttachment.toJson(), msg.getType().getValue(),
					System.currentTimeMillis(), WiFiChat.instance().getMe().getUsername(), user, filestate,
					msgAttachment.getName(), msgAttachment.getUuid(), DBConstants.DOWNLOAD_OK);

			sendMessage(P2pCmd.CMD_FILE_NOTICE, msg);
			if (null != user) {
				Pack pack = PackCommand.getInstance().packReplyMessage(mSelfUserInfo.getUsername(), packet.getCommand(),
						packet.getSn(), data.getMsg_id());
				mDataConnection.sendData(user, pack);
			}
		}
	};

	// 监听实时位置广播
	private PacketListener processBRLoactionListener = new PacketListener() {

		@Override
		public void processPacket(Pack packet) {
			if (TextUtils.isEmpty(packet.getContent()))
				return;

			// BRLoaction loc = GsonUtils.getJson(packet.getContent(),
			// BRLoaction.class);
			BRLoaction loc = new Gson().fromJson(packet.getContent(),BRLoaction.class);
			if (null == loc)
				return;

			UserLocation user = new UserLocation();
			user.setUsername(packet.getSendName());
			user.setLon(loc.getLon());
			user.setLat(loc.getLat());

			sendMessage(P2pCmd.CMD_BR_LOC, user);
		}
	};
	/**
	 * 消息确认回复监听
	 */
	PacketListener processRecvListener = new PacketListener() {

		@Override
		public void processPacket(Pack packet) {
			if (TextUtils.isEmpty(packet.getContent()))
				return;

			ReplyMessage replyMsg = new Gson().fromJson(packet.getContent(), ReplyMessage.class);
			ChatDB.updateSendStatus(WiFiChat.instance().getMe().getUsername(), packet.getSendName(),
					replyMsg.getMsg_id(), DBConstants.SEND_STATUS_OK);
			sendMessage(P2pCmd.CMD_RECV_MSG, replyMsg);
			if (null != observer) {
				Observable.just(packet).subscribeOn(AndroidSchedulers.mainThread()).subscribe(observer);
			}
		}
	};
	private Action1<? super Pack> observer;
	/**
	 * 注册消息回复observer
	 * @param observer
	 */
	public void setMessageObserver(Action1<? super Pack> observer) {
		this.observer = observer;
	}

	public ProcessReceiverData(DataConnection con) {
		mDataConnection = con;
		mService = con.getService();
		mSelfUserInfo = WiFiChat.instance().getMe();
		// 监听上线通知（广播）
		mDataConnection.addPacketListener(new PacketReceiveFilter(P2pCmd.CMD_BR_ENTRY), processEntryListener);
		// 监听上线通知回复
		mDataConnection.addPacketListener(new PacketReceiveFilter(P2pCmd.CMD_ANS_ENTRY), processAnswerEntryListener);
		// 监听下线通知
		mDataConnection.addPacketListener(new PacketReceiveFilter(P2pCmd.CMD_BR_EXIT), processExitListener);
		// 监听文本消息
		mDataConnection.addPacketListener(new PacketReceiveFilter(P2pCmd.CMD_SEND_TEXT), processTextListener);
		// 监听位置消息
		mDataConnection.addPacketListener(new PacketReceiveFilter(P2pCmd.CMD_SEND_LOC), processLocationListener);
		// 监听附件消息
		mDataConnection.addPacketListener(new PacketReceiveFilter(P2pCmd.CMD_FILE_NOTICE), processAttachmentListener);
		// 监听实时位置广播
		mDataConnection.addPacketListener(new PacketReceiveFilter(P2pCmd.CMD_BR_LOC), processBRLoactionListener);
		// 监听消息确认
		mDataConnection.addPacketListener(new PacketReceiveFilter(P2pCmd.CMD_RECV_MSG), processRecvListener);

	}

	public void sendMessage(int what, Object obj) {
		Message msg = mHandle.obtainMessage(what, obj);
		mHandle.sendMessage(msg);
	}

	public void addProcessListener(ProcessListener listener) {
		if (null != listener)
			mProcessListener = listener;
	}

	public void removeProcessListener(ProcessListener listener) {
		if (mProcessListener == listener)
			mProcessListener = null;
	}

	public void addLocationListener(ProcessListener.LocationListener listener) {
		if (null != listener)
			locationListener = listener;
	}

	public void removeLocationListener(ProcessListener.LocationListener listener) {
		if (locationListener == listener)
			locationListener = null;
	}

	private void runVibrator() {
		Ringtone ringtone = RingtoneManager.getRingtone(mService.getApplicationContext(),
				android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
		ringtone.play();

		Vibrator vv = (Vibrator) mService.getSystemService(Context.VIBRATOR_SERVICE);
		vv.vibrate(500);// 震半秒钟
	}
}
