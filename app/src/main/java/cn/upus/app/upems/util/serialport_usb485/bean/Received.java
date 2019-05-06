package cn.upus.app.upems.util.serialport_usb485.bean;

import java.util.Arrays;

/**
 * 下位机返回信息
 */
public class Received {

    /**
     * 指令结果 0 失败 1 成功 2 超时 3 发送命令失败
     */
    private int type;
    /**
     * 是否接收到下位机信息
     */
    private boolean received;
    /**
     * 接收到的信息字节长度
     */
    private int size;
    /**
     * 接收到的数据
     */
    private byte[] buffer;
    /**
     * 转换后的数据
     */
    private String readData;

    public Received() {
    }

    /**
     * @param type     指令结果 0 失败 1 成功 2 超时 3 发送命令失败
     * @param received 是否接收到下位机信息
     * @param size     接收到的信息字节长度
     * @param buffer   接收到的数据
     * @param readData 转换后的数据
     */
    public Received(int type, boolean received, int size, byte[] buffer, String readData) {
        this.type = type;
        this.received = received;
        this.size = size;
        this.buffer = buffer;
        this.readData = readData;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public String getReadData() {
        return readData;
    }

    public void setReadData(String readData) {
        this.readData = readData;
    }

    @Override
    public String toString() {
        return "Received{" +
                "type=" + type +
                ", received=" + received +
                ", size=" + size +
                ", buffer=" + Arrays.toString(buffer) +
                ", readData='" + readData + '\'' +
                '}';
    }
}
