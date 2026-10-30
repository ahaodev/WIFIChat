package com.ahao.wifichat.ui.chat.video;

import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ahao.wifichat.R;
import com.ahao.wifichat.ui.BaseActivity;

public class VideoRecorderActivity extends BaseActivity
		implements SurfaceHolder.Callback, OnClickListener, MediaRecorder.OnInfoListener, OnTouchListener {

	class VideoTimerTask extends TimerTask {

		@Override
		public void run() {
			timeCount++;
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					timeView.setText(secondToTimeMS(timeCount));

				}

				private CharSequence secondToTimeMS(int seconds) {
					String minute;
					String second;
					int ss = seconds;
					int s = ss % 60;
					int m = ss / 60;
					int h = m / 60;
					m = (m < 60) ? m : m % 60;

					minute = (m < 10) ? "0" + m + ":" : m + ":";
					second = (s < 10) ? "0" + s : String.valueOf(s);
					return minute + second;
				}
			});
		}

	}

	private Timer timer = null;

	private SurfaceHolder surfaceHolder;
	private SurfaceView surfaceView;
	public MediaRecorder mrec = null;
	private int timeCount = 0;

	private boolean succeed = false;

	public static final String VIDEO_FILEPATH = "filepath";
	public static final String VIDEO_TIME_LIMIT = "timelimit";
	public static final String VIDEO_QUALITY = "quality";

	private int previewWidth = 320;
	private int previewHeight = 240;

	private int videoRoate = 0;

	private String filePath;
	private int timeLimit = 30;

	private List<Size> videoSizes;

	int defaultCameraId = -1, defaultScreenResolution = -1, cameraSelection = CameraInfo.CAMERA_FACING_BACK;
	int defaultVideoFrameRate = -1;

	private Camera mCamera;

	private Button goBackButton;
	private Button rePlayButton;
	private Button recorderButton;
	private Button switchCameraButton;
	private Button senderButton;

	private TextView timeView;

	private ImageView imgView;

	private boolean isRecording = false;

	private int cameraIndex = CameraInfo.CAMERA_FACING_BACK;
	private int encodeBitRate = 1 * 1024 * 128;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
		// 选择支持半透明模式，在有surfaceview的activity中使用
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.p2p_activity_video_recorder);

		surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		recorderButton = (Button) findViewById(R.id.recoderbutton);
		goBackButton = (Button) findViewById(R.id.go_back_button);
		rePlayButton = (Button) findViewById(R.id.play_button);
		switchCameraButton = (Button) findViewById(R.id.switch_camera_button);
		senderButton = (Button) findViewById(R.id.send_button);

		imgView = (ImageView) findViewById(R.id.img_view);

		timeView = (TextView) findViewById(R.id.recorder_time);

		recorderButton.setOnClickListener(this);
		goBackButton.setOnClickListener(this);
		rePlayButton.setOnClickListener(this);
		switchCameraButton.setOnClickListener(this);
		senderButton.setOnClickListener(this);

		rePlayButton.setVisibility(View.GONE);
		switchCameraButton.setVisibility(View.GONE); // disable
		senderButton.setVisibility(View.GONE);
		timeView.setVisibility(View.GONE);
		goBackButton.setVisibility(View.VISIBLE);
		imgView.setVisibility(View.GONE);

		surfaceView.setOnTouchListener(this);

		Intent intent = this.getIntent();
		filePath = intent.getStringExtra(VIDEO_FILEPATH);
		timeLimit = intent.getIntExtra(VIDEO_TIME_LIMIT, 30);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "StartRecording");
		menu.add(0, 1, 0, "StopRecording");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			try {
				startRecording();
			} catch (Exception e) {
				String message = e.getMessage();
				mrec.release();
				mrec = null;
			}
			break;

		case 1: // GoToAllNotes
			try {
				stopRecording();
			} catch (Exception e) {
				String message = e.getMessage();
				mrec.release();
				mrec = null;
			}
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	class SizeComparator implements Comparator<Size> {

		@Override
		public int compare(Size arg0, Size arg1) {
			int rs = 0;
			if (arg0.width == arg1.width && arg0.height == arg1.height) {
				rs = 0;
			} else if (arg0.width < arg0.width) {
				rs = -1;
			} else if (arg0.width > arg0.width) {
				rs = 1;
			}
			return rs;
		}

	}

	private Size getBestVideoSize() {
		return getOptimalPreviewSize(videoSizes, 640, 480);
	}

	protected void startRecording() {
		// surfaceView.setVisibility(View.VISIBLE);
		if (mCamera == null) {
			return;
		}
		try {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			mCamera.unlock();
			mrec = new MediaRecorder();
			mrec.setCamera(mCamera);

			mrec.setOrientationHint(videoRoate);

			mrec.setPreviewDisplay(surfaceHolder.getSurface());
			mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			mrec.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
			mrec.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mrec.setVideoEncodingBitRate(encodeBitRate);
			mrec.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

			mrec.setVideoSize(previewWidth, previewHeight);

			mrec.setMaxDuration(timeLimit * 1000);
			mrec.setOnInfoListener(this);

			mrec.setPreviewDisplay(surfaceHolder.getSurface());
			mrec.setOutputFile(filePath);

			mrec.prepare();
			mrec.start();

			rePlayButton.setVisibility(View.GONE);
			senderButton.setVisibility(View.GONE);
			timeView.setVisibility(View.VISIBLE);
			timeView.setText("00:00");
			goBackButton.setVisibility(View.GONE);
			imgView.setVisibility(View.GONE);
			surfaceView.setVisibility(View.VISIBLE);
			recorderButton.setText(R.string.video_recorder_stop);

			timeCount = 0;

			timer = new Timer();
			timer.schedule(new VideoTimerTask(), 1000, 1000); // 1s后执行task,经过1s再次执行

			isRecording = true;
		} catch (Exception e) {
			releaseMediaRecorder();
			Toast.makeText(this, R.string.video_recorder_failed_to_start, Toast.LENGTH_LONG).show();
			String message = e.getMessage();
		}
	}

	protected void stopRecording() {
		if (mrec == null) {
			return;
		}
		succeed = true;
		releaseMediaRecorder();
		isRecording = false;
		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
		rePlayButton.setVisibility(View.VISIBLE);
		senderButton.setVisibility(View.VISIBLE);
		goBackButton.setVisibility(View.VISIBLE);
		recorderButton.setText(R.string.video_recorder_start);
		// surfaceView.setVisibility(View.GONE);
		showPreviewImage();
		// switchCameraButton.setVisibility(View.VISIBLE);

		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// try {
		// //mCamera.reconnect();
		// } catch (IOException e) {
		//
		// e.printStackTrace();
		// }
	}

	private void showPreviewImage() {
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource(filePath);
		Bitmap bmp = retriever.getFrameAtTime(-1);
		imgView.setImageBitmap(bmp);
		imgView.setVisibility(View.VISIBLE);
	}

	public void onInfo(MediaRecorder mr, int what, int extra) {
		if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
			stopRecording();
		}
	}

	private void releaseMediaRecorder() {

		if (mrec != null) {
			if (isRecording) {
				mrec.stop();

			}
			mrec.reset(); // clear recorder configuration
			mrec.release(); // release the recorder object
			mrec = null;
		}
		if (mCamera != null && isRecording) {
			mCamera.lock(); // lock camera for later use
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		releaseMediaRecorder();
		releaseCamera();
	}

	@SuppressLint("NewApi")
	protected void initpreview() throws Exception {
		try {

			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
				int numberOfCameras = Camera.getNumberOfCameras();
				CameraInfo cameraInfo = new CameraInfo();
				for (int i = 0; i < numberOfCameras; i++) {
					Camera.getCameraInfo(i, cameraInfo);
					if (cameraInfo.facing == cameraSelection) {
						defaultCameraId = i;
					}
				}

			}
			if (mCamera != null) {
				mCamera.stopPreview();
			}

			mCamera = Camera.open(defaultCameraId);

			if (mCamera != null) {
				// set preview size
				Parameters params = mCamera.getParameters();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					videoSizes = params.getSupportedVideoSizes();
				}
				Size s = getBestVideoSize();
				if (s != null) {
					previewWidth = s.width;
					previewHeight = s.height;
				}

				// set focus mode
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
				} else {
					params.setFocusMode(Parameters.FOCUS_MODE_AUTO);
				}

				params.setPreviewSize(previewWidth, previewHeight);
				try {
					mCamera.setParameters(params);
				} catch (Exception e) {
					params = mCamera.getParameters();
					previewHeight = params.getPreviewSize().height;
					previewWidth = params.getPreviewSize().width;
				}

				// Get the set dimensions
				float newProportion = (float) previewHeight / (float) previewWidth;

				// Get the width of the screen
				int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
				int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
				float screenProportion = (float) screenWidth / (float) screenHeight;
				// Get the SurfaceView layout parameters
				android.view.ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
				if (newProportion > screenProportion) {
					lp.width = screenWidth;
					lp.height = (int) ((float) screenWidth / newProportion);
				} else {
					lp.width = (int) (newProportion * (float) screenHeight);
					lp.height = screenHeight;
				}

				mCamera.setPreviewDisplay(surfaceHolder);
				setCameraDisplayOrientation(mCamera);
				mCamera.startPreview();
			} else {
				Toast.makeText(this, "无法启动照相机", Toast.LENGTH_LONG).show();
			}

		} catch (Exception e) {

			Toast.makeText(this, "无法启动照相机", Toast.LENGTH_LONG).show();
			// throw new Exception(e.getMessage());
		}

	}

	/**
	 * Iterate over supported camera preview sizes to see which one best fits
	 * the dimensions of the given view while maintaining the aspect ratio. If
	 * none can, be lenient with the aspect ratio.
	 *
	 * @param sizes
	 *            Supported camera preview sizes.
	 * @param w
	 *            The width of the view.
	 * @param h
	 *            The height of the view.
	 * @return Best match camera preview size to fit in the view.
	 */
	public static Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		// Use a very small tolerance because we want an exact match.
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;

		// Start with max value and refine as we iterate over available preview
		// sizes. This is the
		// minimum difference between view and camera height.
		double minDiff = Double.MAX_VALUE;

		// Target view height
		int targetHeight = h;

		// Try to find a preview size that matches aspect ratio and the target
		// view size.
		// Iterate over all available sizes and pick the largest size that can
		// fit in the view and
		// still maintain the aspect ratio.
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find preview size that matches the aspect ratio, ignore the
		// requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	// @SuppressLint("NewApi")
	private void setCameraDisplayOrientation(Camera camera) {
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(cameraIndex, info);
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		videoRoate = result;
		camera.setDisplayOrientation(result);
	}

	@Override
	public void onClick(View v) {
		if (v == recorderButton) {
			if (!isRecording) {
				startRecording();
			} else {
				stopRecording();
			}
		} else if (v == goBackButton) {

			setResult(RESULT_CANCELED);
			finish();

		} else if (v == rePlayButton) {
			Intent intent = new Intent(this, WeixinVideoPlayerActivity.class);
			intent.putExtra("url", filePath);
			startActivityForResult(intent, 0);
		} else if (v == switchCameraButton) {
			if (!isRecording) {
				cameraIndex = cameraIndex == CameraInfo.CAMERA_FACING_BACK ? CameraInfo.CAMERA_FACING_FRONT
						: CameraInfo.CAMERA_FACING_BACK;
				releaseCamera();
				try {
					initpreview();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
		} else if (v == senderButton) {
			if (succeed) {
				Intent intent = new Intent(filePath);
				setResult(RESULT_OK, intent);
				finish();
			}

		}
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		super.onBackPressed();

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		try {
			initpreview();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	protected void onRestoreInstanceState(Bundle outState) {

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// outState.putLong("time", sec);
		// outState.putBoolean("isplaying", video.isPlaying());
		// outState.putBoolean("controlbarIsShow", mc.isShown());
	}

	@SuppressLint({ "NewApi", "ClickableViewAccessibility" })
	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (mCamera != null) {
				mCamera.cancelAutoFocus();
				Parameters params = mCamera.getParameters();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					/*
					 * 
					 * ArrayList<Area> areaList = new ArrayList<Area>(); ;
					 * 
					 * float x = event.getX(); float y = event.getY(); float
					 * touchMajor = event.getTouchMajor(); float touchMinor =
					 * event.getTouchMinor(); Rect focusArea = new Rect(); Rect
					 * touchRect = new Rect( (int)(x - touchMajor/2), (int)(y +
					 * touchMinor/2), (int)(x + touchMajor/2), (int)(y -
					 * touchMinor/2)); focusArea.set(touchRect.left * 2000 /
					 * surfaceView.getWidth() - 1000, touchRect.top * 2000 /
					 * surfaceView.getHeight() - 1000, touchRect.right * 2000 /
					 * surfaceView.getWidth() - 1000, touchRect.bottom * 2000 /
					 * surfaceView.getHeight() - 1000);
					 * 
					 * 
					 * Area area = new Area(focusArea, 1000);
					 * 
					 * //params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO)
					 * ;
					 * 
					 * if (params.getMaxNumFocusAreas() > 0) {
					 * areaList.add(area);
					 * 
					 * params.setFocusAreas(areaList); try {
					 * //mCamera.setParameters(params); } catch (Exception e) {
					 * e.printStackTrace(); } }
					 * 
					 * mCamera.autoFocus(new Camera.AutoFocusCallback() {
					 * 
					 * @Override public void onAutoFocus(boolean success, Camera
					 * camera) { //Log.d(TAG, "onAutoFocus() " + success); } });
					 */

				} else {
					mCamera.autoFocus(new Camera.AutoFocusCallback() {
						@Override
						public void onAutoFocus(boolean success, Camera camera) {
							// Log.d(TAG, "onAutoFocus() " + success);
						}
					});

				}
			}
			return true;
		}
		return true;
	}
}