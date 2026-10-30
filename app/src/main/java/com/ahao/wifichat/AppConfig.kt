package com.ahao.wifichat;

/**
 * 配置类
 * 
 * @author hao.ruan
 *
 */
public class AppConfig {
	String videoPath;

	private AppConfig() {

	}

	static AppConfig config = new AppConfig();

	public static AppConfig getInstance() {
		return config;
	}

	public String getVideoPath() {
		return videoPath;
	}

	public void setBackupVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}

}
