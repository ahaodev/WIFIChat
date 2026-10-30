package com.ahao.wifichat.protocol;

public class SerialNumberUtils {
	
	private static int sn = 0;
    
    public synchronized static int getNewNumIdx() {
		++sn;
		if (sn < 0)
			sn = 0;
		return sn;
	}
}
