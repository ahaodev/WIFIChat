package com.ahao.wifichat.ui.chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.ahao.wifichat.R;
import com.ahao.wifichat.WiFiChat;
import com.ahao.wifichat.WifiChatApplication;
import com.ahao.wifichat.db.ChatDB;
import com.ahao.wifichat.db.DBConstants;
import com.ahao.wifichat.db.user_msg;
import com.ahao.wifichat.net.file.FileClientSendListener;
import com.ahao.wifichat.net.file.FileState;
import com.ahao.wifichat.net.file.TcpClient;
import com.ahao.wifichat.protocol.codec.Pack;
import com.ahao.wifichat.service.ProcessListener;
import com.ahao.wifichat.service.parcelable.OnlineUserInfo;
import com.ahao.wifichat.service.parcelable.UserMessage;
import com.ahao.wifichat.ui.BaseActivity;
import com.ahao.wifichat.ui.chat.video.VideoRecorderActivity;
import com.ahao.wifichat.utlis.ImageUtils;
import com.ahao.wifichat.utlis.StorageUntils;
import com.ahao.wifichat.widget.ChatInputView;
import com.ahao.wifichat.widget.TitleView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class ChatActivity extends BaseActivity implements ChatInputView.InputClickListener {
	private static final int REQ_SELECT_VIDEO = 0x0001;// 去相册
	private static final int REQ_TACK_VIDEO = 0x0005;// 去录制
	private static final int REQ_SELECT_LOCATION = 0x0003;// 去选位置
	private static final int REQ_SELECT_FILES = 0x0006;// 去选文件
	private static final int REQUEST_CODE_CHOOSE = 23;
	public OnlineUserInfo user;
	private ChatInputView chatInputView;
	private ListView chatListView;
	// private ChatMessageAdapter chatAdapter;
	public ChatAdapter adapter;
	private IntentFilter intentFilter;
	public static final String USER = "user";
	public List<user_msg> chatMessages = new ArrayList<>();// 聊天消息作为缓存，避免每次来消息都从库查
	private TitleView titleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.p2p_activity_chat);
		titleView = (TitleView) findViewById(R.id.title);
		user = getIntent().getExtras().getParcelable(ChatActivity.USER);
		chatInputView = (ChatInputView) findViewById(R.id.chatInput);
		titleView.setTitle(user.getName());
		if (user.getOnline() == 0) {//不在线
			chatInputView.setVisibility(View.GONE);
		}
		chatInputView.setOnInputClickListener(this);
		chatInputView.setOnAudioRecordListener(new ChatAudioListener(this));
		chatListView = (ListView) findViewById(R.id.p2p_chat_messagelist);
		findViewById(R.id.chat_windo).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				chatInputView.hideGroud();
				chatInputView.hideKeyBode();
			}
		});

		// chatListView.setAdapter(chatAdapter = new ChatMessageAdapter(this,
		// getMessages(), 0));
		chatListView.setAdapter(adapter = new ChatAdapter(this, getMessages()));
		chatListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE://停止滚动
					adapter.setScrollState(false);
					break;
				case OnScrollListener.SCROLL_STATE_FLING:// 滚动做出了抛的动作
					// 设置为正在滚动
					adapter.setScrollState(true);
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 正在滚动
					// 设置为正在滚动
					adapter.setScrollState(true);
					break;
				default:
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});
		// chatListView.setOnItemClickListener(chatAdapter.itemClick);
		// chatAdapter.setUser(user);
		// intentFilter = new IntentFilter(NotifycationManagers.TXT_ACTION);
		// registerReceiver(receiver, intentFilter);
		registerForContextMenu(chatInputView);
		ChatDB.getInstance().updateReadStatus(user.getUserName());
		ChatDB.getInstance().updateSendTimeOut(WiFiChat.instance().getMe().getUsername(), user.getUserName(),
				System.currentTimeMillis() - 10 * 1000);
		tcpClient = TcpClient.getInstance(ChatActivity.this);
		tcpClient.setOnFileListener(sendFileListener);
		tcpClient.startSend();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contextmenu_chat_vedio, menu);// 注册视频按钮的选项
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (null == app.getBinder()) {
			return;
		}
		app.getBinder().getDataConnection().addProcessListener(listener);
		app.getBinder().getDataConnection().setMessageObserver(observer);
		// chatAdapter.notifyDataSetChanged(getMessages());
		adapter.notifyDataSetChanged(getMessages());
	}

	@Override
	protected void onDestroy() {
		// unregisterReceiver(receiver);
		app.getBinder().getDataConnection().removeProcessListener(listener);
		super.onDestroy();
	}

	@Override
	public void sendImage() {
		// 去选择照片
		/*Intent intent = new Intent(this, LocalAlbum.class);
		startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCROP);*/
		RxPermissions rxPermissions = new RxPermissions(this);
		 rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
			 @Override
			 public void onCompleted() {

			 }

			 @Override
			 public void onError(Throwable e) {

			 }

			 @Override
			 public void onNext(Boolean aBoolean) {
//				 Matisse.from(ChatActivity.this)
//						 .choose(MimeType.allOf())
//						 .countable(true)
//						 .capture(true)
//						 .captureStrategy(
//								 new CaptureStrategy(true, "com.ruanhao.wifichat.fileprovider"))
//						 .maxSelectable(9)
//						 .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
//						 .gridExpectedSize(
//								 getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
//						 .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
//						 .thumbnailScale(0.85f)
//						 .imageEngine(new GlideEngine())
//						 .forResult(REQUEST_CODE_CHOOSE);

				 Matisse.from(ChatActivity.this)
				.choose(MimeType.ofImage())
				.theme(com.zhihu.matisse.R.style.Matisse_Dracula)
				.countable(false)
				.maxSelectable(9)
				.imageEngine(new GlideEngine())
				.forResult(REQUEST_CODE_CHOOSE);
			 }
		 });
	}

	@Override
	public void sendVideo() {
		// 去录制视屏或者选择
		chatInputView.showContextMenuForChild(chatInputView);
		chatInputView.findViewById(R.id.p2p_chat_input_grid);
	}

	@Override
	public void sendLocation() {
		// 去选择位置
/*		Intent intent = new Intent(ChatActivity.this, MapSelectLocationActivity.class);
		startActivityForResult(intent, REQ_SELECT_LOCATION);*/
	}

	@Override
	public void sendFile() {
		// 去找文件
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");// 设置类型，我这里是任意类型，任意后缀的可以这样写。
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, REQ_SELECT_FILES);
	}

	@Override
	public void sendMessage(final String message) {
		Observable.just(message).subscribeOn(Schedulers.io()).subscribe(new Action1<String>() {

			@Override
			public void call(String str) {

			}
		});

		boolean tag = app.getBinder().getChatManager().sendText(user, message);
		Logger.e("TAG", tag);
		// app.getBinder().getConversationsMap().put(chatMessage.getName(),
		// chatMessage);// 加入临时的会话列表
		// app.getBinder().getUserHistoricalMessageList(user.getUserName()).add(chatMessage);//
		// 加入历史消息会话

		// chatAdapter.notifyDataSetChanged(getMessages());
		adapter.notifyDataSetChanged(getMessages());
		smoothScroll(adapter.getCount()-1);
	}

	/**
	 * 消息广播接收器
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String Action = intent.getAction();
		}
	};

	private String mImageTempPath;

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != ImageUtils.REQUEST_CODE_GETIMAGE_BYCROP) {
			if (null == data) {
				return;
			}
		}
		switch (requestCode) {
		case REQUEST_CODE_CHOOSE:
/*
			if (LocalImageHelper.getInstance().isResultOk()) {
				LocalImageHelper.getInstance().setResultOk(false);
				// 获取选中的图片
				List<LocalImageHelper.LocalFile> files = LocalImageHelper.getInstance().getCheckedItems();
				for (int i = 0; i < files.size(); i++) {										
					sendImg(files.get(i).getOriginalUri());
				}
				// 清空选中的图片
				files.clear();
			}
			// 清空选中的图片
			LocalImageHelper.getInstance().getCheckedItems().clear();*/

			if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
				List<Uri>	mSelected = Matisse.obtainResult(data);
				for (int i = 0; i < mSelected.size(); i++) {
					//sendImg(mSelected.get(i).getPath());
					sendImg(mSelected.get(i).toString());
				}
			}

			break;
		case REQ_TACK_VIDEO:// 录制的视屏
			String video = data.getAction();
			sendVideo(video);
			break;
		case REQ_SELECT_VIDEO:// 相册返回的
			Uri videoUri = data.getData();
			String videoPath = ImageUtils.getRealFilePath(this, videoUri);
			sendVideo(videoPath);
			break;
		case REQ_SELECT_FILES:
			Uri fileUri = data.getData();
			String filePath = ImageUtils.getRealPathFromURI(this, fileUri);
			sendFile(filePath);
			break;
		case REQ_SELECT_LOCATION:// 后期扩充更多的key
			/*double lon = data.getExtras().getDouble(MapSelectLocationActivity.LOCATION_LONG);
			double lat = data.getExtras().getDouble(MapSelectLocationActivity.LOCATION_LAT);
			app.getBinder().getChatManager().sendLocation(user, lon, lat, "暂未有位置相关字");
			// chatAdapter.notifyDataSetChanged(getMessages());
			adapter.notifyDataSetChanged(getMessages());*/
			break;

		}
	};

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_vedio_tack:// 去录制
			if (StorageUntils.hasStorage(true)) {

				mImageTempPath = createVideoTmp();
				if (mImageTempPath != null) {
					WifiChatApplication.getInstance().appConfig().setBackupVideoPath(mImageTempPath);

					Uri videoUri = Uri.fromFile(new File(mImageTempPath));

					Intent takeVideoIntent = new Intent(ChatActivity.this, VideoRecorderActivity.class);

					takeVideoIntent.putExtra(VideoRecorderActivity.VIDEO_QUALITY, 0); // 设置视频图像质量为高品质
					takeVideoIntent.putExtra(VideoRecorderActivity.VIDEO_TIME_LIMIT, 30); // 设置视频时间30s
					takeVideoIntent.putExtra(VideoRecorderActivity.VIDEO_FILEPATH, videoUri.getPath());
					try {
						startActivityForResult(takeVideoIntent, REQ_TACK_VIDEO);

					} catch (ActivityNotFoundException e) {
						Toast.makeText(ChatActivity.this, "摄像头异常", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(ChatActivity.this, "创建临时文件失败", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(ChatActivity.this, R.string.insert_sd_card, Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.menu_vedio_choose:// 去相册
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("video/*");
			startActivityForResult(photoPickerIntent, REQ_SELECT_VIDEO);
			break;
		case R.id.menu_vedio_cancel:// 取消

			break;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	@SuppressLint("SimpleDateFormat")
	private String createVideoTmp() {
		String path = null;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "VID_" + timeStamp;// + "_";

		File albumF = getExternalFilesDir("VideoFile");
		path = albumF + "/" + imageFileName + ".3gp";
		return path;
	}

	private ProcessListener listener = new ProcessListener() {

		@Override
		public void onOnlineUserUpdate(OnlineUserInfo userInfo) {
			if (null == app.getBinder().getUser(user.getUserName())) {
				Toast.makeText(ChatActivity.this, "对方已经下线", Toast.LENGTH_SHORT).show();
				chatInputView.setVisibility(View.GONE);
			} else {
				chatInputView.setVisibility(View.VISIBLE);
				Toast.makeText(ChatActivity.this, "对方已经上线", Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		public void onNewMessage(UserMessage msg) {
			if (!msg.getSendUsername().equals(user.getUserName())) {
				return;
			}
			// ChatText text = new Gson().fromJson(msg.getContent(),
			// ChatText.class);
			// ChatMessage chatMessage = new ChatMessage();
			// chatMessage.setName(msg.getSendUsername());
			// chatMessage.setMe(1);
			// chatMessage.setType(1);
			// chatMessage.setText(text.getText());
			// chatMessage.setTime(StringUtils.formatTime(msg.getMsg_time()));
			// app.getBinder().getConversationsMap().put(chatMessage.getName(),
			// chatMessage);// 加入临时的会话列表
			// chatAdapter.notifyDataSetChanged(getMessages());
			smoothScroll(adapter.getCount()-1);
			adapter.notifyDataSetChanged(getMessages());
			ChatDB.getInstance().updateReadStatus(user.getUserName());
		}
	};
	private Action1<Pack> observer = new Action1<Pack>() {

		@Override
		public void call(Pack pack) {
			adapter.notifyDataSetChanged(getMessages());
		}
	};

	/**
	 * 获取个人的历史消息
	 * 
	 * @return
	 */
	public List<user_msg> getMessages() {
		return ChatDB.getInstance().queryChatMessage(app.getMe().getUsername(), user.getUserName());
	}

	private TcpClient tcpClient;
	private FileClientSendListener sendFileListener=new FileClientSendListener() {
		
		@Override
		public void onSuccess(FileState state) {
			
		}
		
		@Override
		public void onStart(FileState state) {
			new Handler(getMainLooper()).post(new Runnable() {
				
				@Override
				public void run() {
					adapter.notifyDataSetChanged(getMessages());
					smoothScroll(adapter.getCount() - 1);
				}
			});
		}
		
		@Override
		public void onProgress(int progress) {
			
		}

		@Override
		public void onError(Exception e) {
			
		}
		
	};

	public void sendAudio(final String path) {
		if (TextUtils.isEmpty(path)) {
			return;
		}
		tcpClient.startSend();

		Observable.just(path).subscribeOn(Schedulers.io()).subscribe(new Action1<String>() {

			@Override
			public void call(String str) {
				tcpClient.sendFile(path, user, DBConstants.FILE_TYPE_AUDIO);
			}
		});
		chatInputView.hideGroud();
	}

	public void sendImg(final String path) {
		if (TextUtils.isEmpty(path)) {
			return;
		}
		Observable.just(path).subscribeOn(Schedulers.io()).subscribe(new Action1<String>() {

			@Override
			public void call(String str) {
				
				tcpClient.sendFile(ImageUtils.getRealFilePath(ChatActivity.this, Uri.parse(path)), user, DBConstants.FILE_TYPE_IMG);
			}
		});
		chatInputView.hideGroud();
	}
	public void sendImg(@NotNull  final Uri uri) {
		Observable.just(uri).subscribeOn(Schedulers.io()).subscribe(new Action1<Uri>() {

			@Override
			public void call(Uri uri) {

				tcpClient.sendFile(ImageUtils.getRealFilePath(ChatActivity.this, uri), user, DBConstants.FILE_TYPE_IMG);
			}
		});
		chatInputView.hideGroud();
	}

	public void sendVideo(final String path) {
		if (TextUtils.isEmpty(path)) {
			return;
		}
		Observable.just(path).subscribeOn(Schedulers.io()).subscribe(new Action1<String>() {

			@Override
			public void call(String str) {
				tcpClient.sendFile(path, user, DBConstants.FILE_TYPE_VIDEO);
			}
		});
		chatInputView.hideGroud();
	}

	public void sendFile(final String path) {
		if (TextUtils.isEmpty(path)) {
			return;
		}
		Observable.just(path).subscribeOn(Schedulers.io()).subscribe(new Action1<String>() {

			@Override
			public void call(String str) {
				tcpClient.sendFile(path, user, DBConstants.FILE_TYPE);
			}
		});
		chatInputView.hideGroud();
	}

	public void smoothScroll(final int pos) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				chatListView.smoothScrollToPosition(pos);
			}
		};

		chatListView.post(runnable);
	}
}
