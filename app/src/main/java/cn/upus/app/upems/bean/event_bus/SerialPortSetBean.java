package cn.upus.app.upems.bean.event_bus;

/**
 * 指纹 驱动 信息
 */
public class SerialPortSetBean {

    private int type;

    public SerialPortSetBean(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
