package com.ahao.wifichat.net;

import android.location.Location;
import android.text.TextUtils;


import com.ahao.wifichat.WiFiChat;
import com.ahao.wifichat.entity.Userinfo;
import com.ahao.wifichat.misc.ErrorHandler;
import com.ahao.wifichat.protocol.P2pCmd;
import com.ahao.wifichat.protocol.codec.Codec;
import com.ahao.wifichat.protocol.codec.Pack;
import com.ahao.wifichat.service.P2PService;
import com.ahao.wifichat.service.PacketListener;
import com.ahao.wifichat.service.ProcessListener;
import com.ahao.wifichat.service.ProcessReceiverData;
import com.ahao.wifichat.service.filter.PacketFilter;
import com.ahao.wifichat.service.parcelable.OnlineUserInfo;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import rx.functions.Action1;

/**
 * 
 * @author xiang.shen
 *
 */
public class DataConnection implements ReceiverListener {

	public static int Command_UDP[] = {};

	public static int Command_Multicast[] = { P2pCmd.CMD_BR_ENTRY, P2pCmd.CMD_BR_EXIT, P2pCmd.CMD_BR_LOC };

	protected final Map<PacketListener, ListenerWrapper> recvListeners = new ConcurrentHashMap<PacketListener, ListenerWrapper>();
	/**
	 * ExecutorService used to invoke the PacketListeners on newly arrived and
	 * parsed stanzas. It is important that we use a <b>single threaded
	 * ExecutorService</b> in order to guarantee that the PacketListeners are
	 * invoked in the same order the stanzas arrived.
	 */
	private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);

	private final P2PService mService;
	private final NetworkService mNetworkService;
	private final ErrorHandler mErrorHandler;
	private final ProcessReceiverData mProcessReceiverData;
	private final Userinfo mSelfUserInfo;

	private ExecutorService mExecutor;

	public DataConnection(P2PService service) {
		mService = service;
		mSelfUserInfo = WiFiChat.instance().getMe();
		// 处理接收的数据类
		mProcessReceiverData = new ProcessReceiverData(this);

		mExecutor = Executors.newFixedThreadPool(5);
		mErrorHandler = new ErrorHandler();
		mNetworkService = new NetworkService(mSelfUserInfo, mErrorHandler);
		// mNetworkService.registerMessageReceiverListener(this);
		mNetworkService.registerUDPReceiverListener(this);
		initlize();
	}

	// 初始化
	public void initlize() {
		connect();
	}

	// 销毁
	public void onDestroy() {
		mExecutor.shutdownNow();
		disconnect();
	}

	// 连接网络，准备收发数据
	public void connect() {
		if (!mNetworkService.isConnectionWorkerAlive()) {
			mNetworkService.connect();
		}
	}

	// 断开连接，关闭收发数据
	public void disconnect() {
		mNetworkService.disconnect();
	}

	// 发送数据
	public synchronized Boolean sendData(OnlineUserInfo destUser, Pack pack) {
		if (null == destUser || null == pack)
			return false;

		//编码
		StringBuilder data = Codec.doEncode(pack);
		if (null == data)
			return false;

		return submit(new SendDataUDP(mNetworkService, destUser, data.toString()));
	}

	public synchronized void broadcastLocation(Location location){
		
	}
	//发送广播消息
	public boolean sendData(Pack pack) {
		OnlineUserInfo user = new OnlineUserInfo();
		user.setIpAdderss(Constants.NETWORK_BROADCAST_IP);
		user.setPort(Constants.NETWORK_UDP_PORT);
		return sendData(user, pack);
	}

	public Boolean submit(Callable call) {
		if (null == call)
			return false;
		if (mExecutor.isShutdown()) {
			return false;
		}
		return mExecutor.submit(call).isDone();
	}

	public P2PService getService() {
		return mService;
	}

	@Override
	public synchronized void messageArrived(String message, String ipAddress, int port) {
		// 过滤自己发送的消息
		if (TextUtils.equals(mSelfUserInfo.getIpAdderss(), ipAddress))
			return;
		Pack pack = Codec.doDecode(message);
		if (null == pack)
			return;

		processPacket(pack);
	}

	// 处理接收的消息
	public void processPacket(Pack pack) {
		if (pack == null) {
			return;
		}

		// Loop through all collectors and notify the appropriate ones.
		// for (PacketCollector collector: getPacketCollectors()) {
		// collector.processPacket(pack);
		// }

		// Deliver the incoming Event to listeners.
		executorService.submit(new ListenerNotification(pack));
	}

	// 增加activity通知
	public void addProcessListener(ProcessListener listener) {
		if (null != listener)
			mProcessReceiverData.addProcessListener(listener);
	}

	// 删除activity通知
	public void removeProcessListener(ProcessListener listener) {
		if (null != listener)
			mProcessReceiverData.removeProcessListener(listener);
	}
	//广播位置监听添加
	public void addLocationListener(ProcessListener.LocationListener listener) {
		if (null != listener)
			mProcessReceiverData.addLocationListener(listener);
	}
	//广播位置监听移除
	public void removeLocationListener(ProcessListener.LocationListener listener) {
		if (null != listener)
			mProcessReceiverData.removeLocationListener(listener);
	}
	// 增加接收消息通知
	public void addPacketListener(PacketFilter packetFilter, PacketListener packetListener) {
		if (packetListener == null) {
			throw new NullPointerException("Event listener is null.");
		}
		ListenerWrapper wrapper = new ListenerWrapper(packetFilter, packetListener);
		recvListeners.put(packetListener, wrapper);
	}

	// 删除接收消息通知
	public boolean removePacketListener(PacketListener packetListener) {
		return recvListeners.remove(packetListener) != null;
	}
	public void setMessageObserver(Action1<? super Pack> observer){
		mProcessReceiverData.setMessageObserver(observer);
	}
	@Override
	protected void finalize() throws Throwable {
		try {
			// It's usually not a good idea to rely on finalize. But this is the
			// easiest way to
			// avoid the "Smack Listener Processor" leaking. The thread(s) of
			// the executor have a
			// reference to their ExecutorService which prevents the
			// ExecutorService from being
			// gc'ed. It is possible that the XMPPConnection instance is gc'ed
			// while the
			// listenerExecutor ExecutorService call not be gc'ed until it got
			// shut down.
			executorService.shutdownNow();
		} finally {
			super.finalize();
		}
	}

	private class ListenerNotification implements Runnable {

		private Pack pack;

		public ListenerNotification(Pack temp) {
			this.pack = temp;
		}

		public void run() {
			for (ListenerWrapper listenerWrapper : recvListeners.values()) {
				listenerWrapper.notifyListener(pack);
			}
		}
	}

	/**
	 * A wrapper class to associate a Event filter with a listener.
	 */
	protected static class ListenerWrapper {

		private PacketFilter packetFilter;
		private PacketListener packetListener;

		/**
		 * Create a class which associates a Event filter with a listener.
		 *
		 * @param packetListener
		 *            the Event listener.
		 * @param packetFilter
		 *            the associated filter or null if it listen for all
		 *            packets.
		 */
		public ListenerWrapper(PacketFilter packetFilter, PacketListener packetListener) {
			this.packetFilter = packetFilter;
			this.packetListener = packetListener;
		}

		/**
		 * Notify and process the Event listener if the filter matches the
		 * Event
		 */
		public void notifyListener(Pack pack) {
			if (null != packetFilter && packetFilter.accept(pack)) {
				packetListener.processPacket(pack);
			}
		}
	}
}
