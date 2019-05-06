package cn.upus.app.upems.util.serialport_usb485.usb485;

import android.content.Context;
import android.hardware.usb.UsbManager;

import com.blankj.utilcode.util.LogUtils;

import java.util.concurrent.Semaphore;

import cn.upus.app.upems.MApp;
import cn.upus.app.upems.util.serialport_usb485.StringUtil;
import cn.upus.app.upems.util.serialport_usb485.bean.Received;
import cn.wch.ch34xuartdriver.CH34xUARTDriver;

/**
 * USB 485 通信
 */
public class USB485Util {

    private final String TAG = USB485Util.class.getSimpleName();
    private final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";
    private CH34xUARTDriver driver;
    /*驱动是否开启*/
    public boolean driverOpen = false;
    /*后台线程一直读取 USB 信息*/
    private ReadThread mReadThread;

    /*写入信息后的返回信息*/
    public String readData = null;
    private boolean mReceived = false;
    private byte[] mBuffer = null;
    private int mSize = -1;

    private final int SLEEPTIME = 50;

    /**
     * 信号量
     */
    private Semaphore semaphore;

    public USB485Util() {
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

    /**
     * @param baudrate
     */
    public boolean start(int baudrate) {
        driver = new CH34xUARTDriver((UsbManager) MApp.getApplication.getSystemService(Context.USB_SERVICE), MApp.getApplication, ACTION_USB_PERMISSION);
        if (!driver.UsbFeatureSupported()) {
            LogUtils.e(TAG, "您的设备不支持USB HOST！");
            return false;
        }
        if (!driverOpen) {
            int retval = driver.ResumeUsbList();
            switch (retval) {
                case -1:
                    driver.CloseDevice();
                    driverOpen = false;
                    LogUtils.e(TAG, "串口设备关闭");
                    break;
                case 0://对串口设备进行初始化操作
                    LogUtils.e(TAG, "对串口设备进行初始化操作 1");
                    try {
                        if (!driver.UartInit()) {
                            driverOpen = false;
                            LogUtils.e(TAG, "串口设备初始化失败");
                        } else {
                            driverOpen = true;
                            LogUtils.e(TAG, "串口设备初始化成功");
                        }
                    } catch (Exception e) {
                        LogUtils.e(TAG, "对串口设备进行初始化操作 发生异常:" + e.getMessage());
                        e.printStackTrace();
                    }
                    LogUtils.e(TAG, "对串口设备进行初始化操作 2");
                    break;
                default:
                    LogUtils.e(TAG, "应用USB未授权限");
                    break;
            }
        } else {
            driver.CloseDevice();
            driverOpen = false;
            LogUtils.e(TAG, "openDevice driverOpen true 串口设备关闭");
        }
        if (!driverOpen) {
            return false;
        }
        byte dataBit = 8;
        byte stopBit = 1;
        byte parity = 0;
        byte flowControl = 0;
        if (setConfig(baudrate, dataBit, stopBit, parity, flowControl)) {
            if (null == mReadThread) {
                mReadThread = new ReadThread();
            } else {
                mReadThread.setStop(true);
                mReadThread.interrupt();
                mReadThread = null;
                mReadThread = new ReadThread();
            }
            mReadThread.start();
            return true;
        }
        return false;
    }

    /**
     * 设置波特率
     *
     * @param baudRate
     * @param dataBit
     * @param stopBit
     * @param parity
     * @param flowControl
     */
    public boolean setConfig(int baudRate, byte dataBit, byte stopBit, byte parity, byte flowControl) {
        //2 配置串口波特率，函数说明可参照编程手册
        LogUtils.e(TAG, "setConfig");
        return driver.SetConfig(baudRate, dataBit, stopBit, parity, flowControl);
    }

    /**
     * 关闭 驱动 停止信息读取
     */
    public void close() {
        if (null != mReadThread) {
            mReadThread.setStop(true);
            mReadThread.interrupt();
            mReadThread = null;
        }
        if (null != driver) {
            driver.CloseDevice();
        }
        driverOpen = false;
    }

    /**
     * 发送信息
     *
     * @param data
     * @return
     */
    public synchronized boolean sendData(byte[] data) {
        try {
            LogUtils.e(TAG, StringUtil.toHexString(data, data.length));
            int retval = driver.WriteData(data, data.length);
            if (retval < 0) {
                LogUtils.e(TAG, " 写失败");
                return false;
            } else {
                LogUtils.e(TAG, " 写成功");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, " 写异常 " + e.getMessage());
        }
        return false;
    }


    class ReadThread extends Thread {

        boolean stop = false;

        public void setStop(boolean stop) {
            this.stop = stop;
        }

        @Override
        public void run() {
            super.run();
            byte[] buffer = new byte[4096];
            while (!isInterrupted()) {
                if (stop) {
                    break;
                }
                if (null != driver) {
                    int length = driver.ReadData(buffer, 4096);
                    if (length > 0) {
                        mBuffer = buffer;
                        mSize = length;
                        mReceived = true;
                        readData = StringUtil.toHexString(buffer, length).replace(" ", "");
                        LogUtils.e(TAG, "接收到USB485信息：" + readData);
                    }
                }
            }
        }
    }

}
