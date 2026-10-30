package com.ahao.wifichat.utlis;

import java.io.File;

public class FileUtils {
	/**
	 * 通过判断文件是否存在
	 * 
	 * @param path
	 * @return
	 */

	public static boolean isFileExists(String path) {
		try {
			File file = new File(path);
			if (!file.exists()) {
				return false;
			}

		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		return true;
	}
}
