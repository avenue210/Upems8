package cn.upus.app.upems.bean.event_bus;

/**
 * 发送 指令 处理结果
 */
public class OpenLockCallBackBean {

    /**
     * 返回 1 成功 / 0 失败 / 2 超时 / 3 USB 485 发送失败 / 4 串口发送失败
     */
    private int type;
    private String readData;
    private String message;

    public OpenLockCallBackBean() {
    }

    /**
     * 返回 0 成功 / 1 失败 / 2 超时 / 3 USB 485 发送失败 / 4 串口发送失败
     *
     * @param type
     * @param readData 串口读取的信息
     * @param message
     */
    public OpenLockCallBackBean(int type, String readData, String message) {
        this.type = type;
        this.readData = readData;
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getReadData() {
        return readData;
    }

    public void setReadData(String readData) {
        this.readData = readData;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "OpenLockCallBackBean{" +
                "type=" + type +
                ", readData='" + readData + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
