package cn.upus.app.upems.util.serialport_usb485.serialport;

import android.serialport.SerialPort;
import android.serialport.SerialPortFinder;

import com.blankj.utilcode.util.LogUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.concurrent.Semaphore;

import cn.upus.app.upems.util.serialport_usb485.StringUtil;
import cn.upus.app.upems.util.serialport_usb485.bean.Received;

/**
 * 串口通信
 */
public class SerialportUtil {

    private final String TAG = SerialportUtil.class.getSimpleName();

    //串口
    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private SerialPort mSerialPort = null;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;

    public boolean driverOpen = false;//串口地址 波特率获取失败

    public String readData = null;
    private boolean mReceived = false;
    private byte[] mBuffer = null;
    private int mSize = -1;

    private final int SLEEPTIME = 50;

    /**
     * 信号量
     */
    private Semaphore semaphore;

    public SerialportUtil() {
        semaphore = new Semaphore(1);
    }

    public Received callBack(byte[] buffer) {
        try {
            semaphore.acquire();
            mReceived = false;
            mBuffer = null;
            mSize = -1;
            readData = null;
            if (sendData(buffer)) {
                int n = 5000;
                while (!mReceived && n > 0) {
                    try {
                        Thread.sleep(SLEEPTIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    n -= SLEEPTIME;
                }
                if (mReceived && null != mBuffer && mSize > 0) {
                    byte[] rtBuffer = new byte[mSize];
                    int rtSize = mSize;
                    String read = readData;
                    System.arraycopy(mBuffer, 0, rtBuffer, 0, rtSize);
                    Received rt = new Received(1, mReceived, rtSize, rtBuffer, read);
                    semaphore.release();
                    return rt;
                } else {
                    Received rt = new Received(2, false, 0, null, null);
                    semaphore.release();
                    return rt;
                }
            } else {
                Received rt = new Received(3, false, 0, null, null);
                semaphore.release();
                return rt;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Received rt = new Received(0, false, 0, null, null);
        semaphore.release();
        return rt;
    }

    public boolean start(String path, String baudrate) {
        try {
            mSerialPort = getSerialPort(path, baudrate);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            /* Create a receiving thread */
            mReadThread = new ReadThread();
            mReadThread.start();
            driverOpen = true;
            LogUtils.e(TAG, "串口连接成功");
            return true;
        } catch (IOException e) {
            driverOpen = false;
        }
        return false;
    }

    /**
     * 关闭串口
     */
    public void close() {
        if (null != mReadThread) {
            mReadThread.setStop(true);
            mReadThread.interrupt();
            mReadThread = null;
        }
        if (mSerialPort != null) {
            mOutputStream = null;
            mInputStream = null;
            mSerialPort.close();
            mSerialPort = null;
        }
        driverOpen = false;
    }

    /**
     * 发送串口信息
     *
     * @param mBuffer
     */
    public synchronized boolean sendData(byte[] mBuffer) {
        if (mOutputStream != null) {
            try {
                mOutputStream.write(mBuffer);
                mOutputStream.flush();
                LogUtils.e(TAG, "串口发送成功:" + StringUtil.toHexString(mBuffer, mBuffer.length));
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "串口发送异常");
            }
        } else {
            LogUtils.d(TAG, "串口初始化失败");
        }
        return false;
    }

    /**
     * 获取串口实例
     *
     * @return
     * @throws SecurityException
     * @throws IOException
     * @throws InvalidParameterException
     */
    public SerialPort getSerialPort(String path, String baudrate) throws IOException {
        if (mSerialPort == null) {
            //String path = MApp.mSp.getString(Data.DEVICE);
            //int baudrate = Integer.decode(MApp.mSp.getString(Data.BAUDRATE, "-1"));
            /* Check parameters */
            //if ((path.length() == 0) || (baudrate == -1)) {
            //    throw new InvalidParameterException();
            //}
            /* Open the serial port */
            mSerialPort = new SerialPort(new File(path), Integer.valueOf(baudrate), 0);
        }
        return mSerialPort;
    }

    public String xbxMsg;

    class ReadThread extends Thread {

        private boolean stop = false;

        public void setStop(boolean stop) {
            this.stop = stop;
        }

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                if (stop) {
                    break;
                }
                if (mInputStream == null) {
                    return;
                }
                try {
                    byte[] buffer = new byte[1024];
                    int length = mInputStream.read(buffer);
                    if (length > 0) {
                        mBuffer = buffer;
                        mSize = length;
                        mReceived = true;
                        readData = StringUtil.toHexString(buffer, length).replace(" ", "");
                        xbxMsg = readData;
                        LogUtils.e(TAG, "接收到串口信息：" + readData);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(SLEEPTIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
