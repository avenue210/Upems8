package cn.upus.app.upems.bean.event_bus;

/**
 * 扫码注册
 */
public class ScanCodeRegistrationCallBackBean {

    private int type;

    public ScanCodeRegistrationCallBackBean(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
