package com.ahao.wifichat.utlis;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

@SuppressLint("SimpleDateFormat")
public class AudioPlayer implements OnCompletionListener {

	public interface onPlayListener {
		void onStop(String path);
	}

	private String mRecordFile;
	private boolean bRun;

	private MediaPlayer mPlayer;
	private onPlayListener mPlayListener;

	public AudioPlayer(onPlayListener playListener) {
		mPlayListener = playListener;
	}

	public void startPlay(String path) {
		bRun = true;


		release();

		mRecordFile = path;
		mPlayer = new MediaPlayer();
		try {
			mPlayer.reset();
			mPlayer.setDataSource(path);
			mPlayer.prepare();
			mPlayer.start();
			mPlayer.setOnCompletionListener(this);
		} catch (Exception e) {
			release();
		}
	}

	public void changePlay() {
		if (mPlayer == null || !isRun())
			return;


		try {
			mPlayer.seekTo(mPlayer.getCurrentPosition() - 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopPlay() {
		if (mPlayer == null || !isRun())
			return;


		release();
		bRun = false;
		mRecordFile = null;
	}

	public String getRecordPath() {
		return mRecordFile;
	}

	public boolean isRun() {
		return bRun;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		bRun = false;
		String path = mRecordFile;
		mRecordFile = null;


		if (mPlayListener != null)
			mPlayListener.onStop(path);
	}

	private void release() {
		if (mPlayer != null) {
			try {
				mPlayer.release();
				mPlayer = null;
			} catch (Exception e) {
			}
		}
	}
}
