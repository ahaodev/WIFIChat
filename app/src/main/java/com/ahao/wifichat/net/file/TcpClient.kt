package com.ahao.wifichat.net.file;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.UUID;



import android.content.Context;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.ahao.wifichat.WiFiChat;
import com.ahao.wifichat.WifiChatApplication;
import com.ahao.wifichat.db.ChatDB;
import com.ahao.wifichat.db.DBConstants;
import com.ahao.wifichat.net.Constants;
import com.ahao.wifichat.protocol.v1.MsgAttachment;
import com.ahao.wifichat.service.parcelable.OnlineUserInfo;

public class TcpClient implements Runnable {
	private OnlineUserInfo user;
	private Thread mThread;
	private boolean IS_THREAD_STOP = false; // 是否线程开始标志
	private boolean SEND_FLAG = false; // 是否发送广播标志
	private static TcpClient instance;
	// private ArrayList<FileStyle> fileStyles;
	// private ArrayList<FileState> fileStates;
	private ArrayList<SendFileThread> sendFileThreads;
	private SendFileThread sendFileThread;

	private TcpClient() {
		sendFileThreads = new ArrayList<SendFileThread>();
		mThread = new Thread(this);
		Log.e(this.getClass().getSimpleName(),"建立成功");

	}

	public Thread getThread() {
		return mThread;
	}

	/**
	 * <p>
	 * 获取TcpService实例
	 * <p>
	 * 单例模式，返回唯一实例
	 */
	public static TcpClient getInstance(Context context) {
		if (instance == null) {
			instance = new TcpClient();
		}
		return instance;
	}

	private TcpClient(Context context) {
		this();
		Logger.d("TCP_Client初始化完毕");
	}

	public void startSend() {
		Logger.d("发送线程开启");
		IS_THREAD_STOP = false; // 使能发送标识
		if (!mThread.isAlive())
			mThread.start();
	}

	public void sendFile(String filePath, String target_IP) {
		SendFileThread sendFileThread = new SendFileThread(target_IP, filePath);
		while (SEND_FLAG == true)
			;
		sendFileThreads.add(sendFileThread);
		SEND_FLAG = true;
	}

	public void sendFile(String filePath, OnlineUserInfo user, int type) {
		this.user = user;
		SendFileThread sendFileThread = new SendFileThread(user.getIpAdderss(), filePath, type);
		while (SEND_FLAG == true)
			;
		sendFileThreads.add(sendFileThread);
		FileState sendFileState = new FileState(filePath);
		WifiChatApplication.sendFileStates.put(filePath, sendFileState);// 全局可访问的文件发送状态读取
		SEND_FLAG = true;
	}

	@Override
	public void run() {

		Logger.d("TCP_Client初始化");

		while (!IS_THREAD_STOP) {
			if (SEND_FLAG) {
				for (SendFileThread sendFileThread : sendFileThreads) {
					sendFileThread.start();
				}
				sendFileThreads.clear();
				SEND_FLAG = false;
			}

		}
	}

	public void release() {
		while (SEND_FLAG == true)
			;
		while (sendFileThread.isAlive())
			;
		IS_THREAD_STOP = false;
	}

	public class SendFileThread extends Thread {

		private boolean SEND_FLAG = true; // 是否发送广播标志
		private byte[] mBuffer = new byte[Constants.READ_BUFFER_SIZE]; // 数据报内容
		private OutputStream output = null;
		private DataOutputStream dataOutput;
		private FileInputStream fileInputStream;
		private Socket socket = null;
		private String target_IP;
		private String filePath;
		private int type;

		public SendFileThread(String target_IP, String filePath) {
			this.target_IP = target_IP;
			this.filePath = filePath;
		}

		public SendFileThread(String target_IP, String filePath, int type) {
			this(target_IP, filePath);
			this.type = type;
		}

		public void sendFile() {
			int readSize = 0;
			try {
				socket = new Socket(target_IP, Constants.TCP_SERVER_RECEIVE_PORT);
				fileInputStream = new FileInputStream(new File(filePath));
				output = socket.getOutputStream();
				dataOutput = new DataOutputStream(output);
				int fileSize = fileInputStream.available();
				dataOutput.writeUTF(filePath.substring(filePath.lastIndexOf(File.separator) + 1) + "!" + fileSize + "!"
						+ WiFiChat.instance().getMe().getUsername() + "!" + type);
				long length = 0;

				FileState fs = WifiChatApplication.sendFileStates.get(filePath);
				fs.fileSize = fileSize;
				fs.type = type;
				/**
				 * 插入发送记录
				 */
				UUID uuid = UUID.randomUUID();
				MsgAttachment att = new MsgAttachment();
				att.setUuid(uuid.toString());
				att.setType(fs.type);
				att.setName(fs.fileName);
				att.setSize(fs.fileSize);
				int msg_id = ChatDB.getInstance().insertMessage(DBConstants.DIR_TYPE_I, att.toJson(),
						DBConstants.MSG_TYPE_FILE, System.currentTimeMillis(),
						WiFiChat.instance().getMe().getUsername(), user, fs, filePath, uuid.toString(), 0);
				if (null!=listener) {
					listener.onStart(fs);
				}
				while (-1 != (readSize = fileInputStream.read(mBuffer))) {
					length += readSize;
					dataOutput.write(mBuffer, 0, readSize);
					fs.percent = (int) (length * 100 / fileSize);
					if (null!=listener) {
						listener.onProgress((int) (length * 100 / fileSize));
					}
				}
				Logger.d(fs.fileName + "发送完毕");
				
				WiFiChat.instance().getBinder().getChatManager().sendFileUdp(user, uuid.toString(), filePath, 1, fs,
						msg_id);
				if (null!=listener) {
					listener.onSuccess(fs);
				}
				output.close();
				dataOutput.close();
				socket.close();
				WifiChatApplication.sendFileStates.remove(fs.fileName);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				Logger.d("建立客户端socket失败");
				SEND_FLAG = false;
				e.printStackTrace();
				if (null!=listener) {
					listener.onError(e);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Logger.d("建立客户端socket失败");
				SEND_FLAG = false;
				e.printStackTrace();
				if (null!=listener) {
					listener.onError(e);
				}
			} finally {
				// IS_THREAD_STOP=true;
			}
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Logger.d("SendFileThread初始化");
			if (SEND_FLAG) {
				sendFile();
			}
		}
	}

	FileClientSendListener listener;

	public void setOnFileListener(FileClientSendListener listener) {
		this.listener = listener;
	}
}
