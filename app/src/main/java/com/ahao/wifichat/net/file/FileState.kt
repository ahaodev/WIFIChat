package com.ahao.wifichat.net.file;

public class FileState {
	public long fileSize = 0;
	public long currentSize = 0;
	public String fileName = null;
	public int percent = 0;
	public int type ;

	public FileState() {

	}

	public FileState(String fileFullPath) {
		this.fileName = fileFullPath;
	}

	public FileState(String fileFullPath, int type) {
		this(fileFullPath);
		this.type = type;
	}

	public FileState(long fileSize, long currentSize, String fileName) {
		this.fileSize = fileSize;
		this.currentSize = currentSize;
		this.fileName = fileName;
	}

	public FileState(long fileSize, long currentSize, String fileName, int type) {
		this(fileSize, currentSize, fileName);
		this.type = type;
	}
}
