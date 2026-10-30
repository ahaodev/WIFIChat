package com.ahao.wifichat.net.file;

public interface FileClientSendListener {
	void onStart(FileState state);
	void onProgress(int progress);
	void onSuccess(FileState state);
	void onError(Exception e);
}
