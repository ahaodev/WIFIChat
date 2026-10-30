package com.ahao.wifichat.utlis;

import java.io.File;
import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaRecorder;

@SuppressLint("SimpleDateFormat")
public class AudioRecoder {

	private long startTime;
	private long mDuration;
	private String mRecordFile;
	private boolean bRun;

	private MediaRecorder mRecorder;

    public AudioRecoder(){

    }

	public void startRecord(Context ctx) {
        bRun = true;


 		/* 设置输出文件的路径 */
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String date = sDateFormat.format(new java.util.Date());
        File dir = ctx.getExternalFilesDir("chat");
        File file = new File(dir, "RecordFile_" + date + ".amr");
        mRecordFile = file.getAbsolutePath();
        if (file.exists()) {
            file.delete();
        }

        release();

        mRecorder = new MediaRecorder();
        try {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setMaxDuration(60*1000);
            mRecorder.setOutputFile(mRecordFile);
            mRecorder.prepare();
            mRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
            release();
            bRun = false;
            return;
        }
		mDuration = 0;
		startTime = System.currentTimeMillis();

	}

	public void stopRecord() {
		if (mRecorder == null || !bRun)
			return;

		mDuration = (long) (Math.ceil(System.currentTimeMillis() - startTime) / 1000.0);

		try {
			mRecorder.stop();
            release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		bRun = false;
	}
    public String getRecordPath() {
		return mRecordFile;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getDuration() {
		return mDuration;
	}

	public boolean isRun() {
		return bRun;
	}
	
	public int getAmplitude() {
		if(mRecorder != null) {
			return mRecorder.getMaxAmplitude();
		}
		return 0;
	}

    private void release(){
        if(mRecorder != null){
            try{
                mRecorder.release(); //释放录音资源
                mRecorder = null;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
