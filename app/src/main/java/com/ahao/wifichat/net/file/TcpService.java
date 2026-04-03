package com.ahao.wifichat.net.file;


import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


import android.content.Context;
import android.os.Handler;

import com.orhanobut.logger.Logger;
import com.ahao.wifichat.WifiChatApplication;
import com.ahao.wifichat.db.DBConstants;
import com.ahao.wifichat.net.Constants;

public class TcpService implements Runnable {

    private ServerSocket serviceSocket;
    private boolean SCAN_FLAG = false; // 接收扫描标识
    private Thread mThread;
    ArrayList<FileState> receivedFileNames;
    ArrayList<SaveFileToDisk> saveFileToDisks;
    private static Handler mHandler;
    private String filePath = null; // 存放接收文件的路径

    private static TcpService instance; // 唯一实例

    private boolean IS_THREAD_STOP = false; // 是否线程开始标志

    private TcpService() {
        try {
            serviceSocket = new ServerSocket(Constants.TCP_SERVER_RECEIVE_PORT);
            saveFileToDisks = new ArrayList<SaveFileToDisk>();
            Logger.d("建立监听服务器ServerSocket成功");
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            Logger.d("建立监听服务器ServerSocket失败");
            e.printStackTrace();
        }
        mThread = new Thread(this);
    }

    /**
     * <p>
     * 获取TcpService实例
     * <p>
     * 单例模式，返回唯一实例
     */
    public static TcpService getInstance(Context context) {
        if (instance == null) {
            instance = new TcpService();
        }
        return instance;
    }

    public static void setHandler(Handler paramHandler) {
        mHandler = paramHandler;
    }

    public void setSavePath(String fileSavePath) {
        Logger.d("设置存储路径成功,路径为" + fileSavePath);
        this.filePath = fileSavePath;
        // REV_FLAG=true;
    }

    public TcpService(Context context) {
        this();
    }

    private void scan_recv() {
        try {
            Socket socket = serviceSocket.accept(); // 接收UDP数据报
            // socket.setSoTimeout(5000); // 设置掉线时间
            Logger.d("客户端连接成功");

            SaveFileToDisk fileToDisk = new SaveFileToDisk(socket, filePath);
            fileToDisk.start();

        }
        catch (IOException e) {
            e.printStackTrace();
            Logger.d("客户端连接失败");
            SCAN_FLAG = false;
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        Logger.d("TCP_Service线程开启");
        while (!IS_THREAD_STOP) {
            if (SCAN_FLAG) {
                scan_recv();

            }
        }
    }

    public void release() {
        if (null != serviceSocket && !serviceSocket.isClosed())
            try {
                serviceSocket.close();
                serviceSocket = null;
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        while (SCAN_FLAG == true)
            ;// 直到SCAN_FLAG为false的时候退出循环
        SCAN_FLAG = false;
        IS_THREAD_STOP = true;
    }

    public void startReceive() {
        SCAN_FLAG = true; // 使能扫描接收标识
        if (!mThread.isAlive())
            mThread.start(); // 开启线程
    }

    public void startReceive(ArrayList<FileState> receivedFileNames) {
        SCAN_FLAG = true; // 使能扫描接收标识
        if (!mThread.isAlive())
            mThread.start(); // 开启线程
        this.receivedFileNames = receivedFileNames;
    }

    public void stopReceive() {
        while (SCAN_FLAG == true)
            ;
        SCAN_FLAG = false; // 失能扫描接收标识
    }

    public class SaveFileToDisk extends Thread {
        private boolean SCAN_RECIEVE = true;
        private InputStream input = null;
        private DataInputStream dataInput;
        private byte[] mBuffer = new byte[Constants.READ_BUFFER_SIZE];// 声明接收数组
        private String savePath;
        private int type=1;

        public SaveFileToDisk(Socket socket) {
            try {
                input = socket.getInputStream();
                dataInput = new DataInputStream(input);
                Logger.d("获取网络输入流成功");
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                Logger.d("获取网络输入流失败");
                SCAN_RECIEVE = false;
                e.printStackTrace();
            }
        }

        public SaveFileToDisk(Socket socket, String savePath) {
            this(socket);
            this.savePath = savePath;
        }

        public void recieveFile() {
            int readSize = 0;
            FileOutputStream fileOutputStream = null;
            BufferedOutputStream bufferOutput = null;
            String strFiledata;
            String[] strData = null;
            String fileSavePath;

            try {
                strFiledata = dataInput.readUTF().toString();
                strData = strFiledata.split("!");
                long length = Long.parseLong(strData[1]);// 文件大小

                Logger.d("传输文件类型:" + strData[3]);
                String fileDirPath=savePath +File.separator+strData[2];
        		File fileDir = new File(fileDirPath);
        		if (!fileDir.exists()) {
        			fileDir.mkdirs();
        		}
                fileSavePath = savePath + File.separator + strData[2] + File.separator + strData[0];
                File saveFile =new File(fileDir, strData[0]);
        		fileSavePath=saveFile.getAbsolutePath();
        		
                fileOutputStream = new FileOutputStream(new File(fileSavePath));// 创建文件流
                Logger.d("文件存储路径:" + fileSavePath);
                FileState fileState = new FileState(length, 0, fileSavePath,type);
                WifiChatApplication.recieveFileStates.put(fileSavePath, fileState);
                FileState fs = WifiChatApplication.recieveFileStates.get(fileSavePath);
                bufferOutput = new BufferedOutputStream(fileOutputStream);// 创建带缓冲区的文件流
                long currentLength = 0;
                int count = 0;
                while (-1 != (readSize = dataInput.read(mBuffer))) {
                    bufferOutput.write(mBuffer, 0, readSize);
                    currentLength += readSize;
                    count++;
                    if (count % 10 == 0) {
                        //long Length = currentLength - lastLength;
                        fs.currentSize = currentLength;
                        fs.percent = (int) ((float) currentLength / (float) length * 100);

                        switch (fs.type) {
                            case DBConstants.FILE_TYPE_IMG:
                                break;

                            case DBConstants.FILE_TYPE_AUDIO:
                                break;

                            case DBConstants.FILE_TYPE_VIDEO:
                                android.os.Message msg = mHandler.obtainMessage();
                                msg.obj = fs;
                                msg.sendToTarget();
                                break;

                            default:
                                break;
                        }
                    }
                }

                // 将byte数组的数据写进指定路径
                bufferOutput.flush();

                input.close();
                dataInput.close();
                bufferOutput.close();
                fileOutputStream.close();

                switch (fs.type) {
                    case DBConstants.FILE_TYPE_IMG:
                        break;

                    case DBConstants.FILE_TYPE_AUDIO:
                        break;

                    case DBConstants.FILE_TYPE_VIDEO:
                        android.os.Message msg = mHandler.obtainMessage();
                        fs.percent = 100;
                        msg.obj = fs;
                        msg.sendToTarget();
                        break;

                    default:
                        break;
                }

                WifiChatApplication.recieveFileStates.remove(fs.fileName);
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                Logger.d("写入文件失败");
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            super.run();
            Logger.d("SaveFileToDisk线程开启");
            if (SCAN_RECIEVE)
                recieveFile();
        }
    }
}
