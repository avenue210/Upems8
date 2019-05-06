package cn.upus.app.upems.bean.event_bus;

/**
 * 扫码登录
 */
public class ScanCodeLoginCallBackBean {

    private int type;
    private String json;

    public ScanCodeLoginCallBackBean(int type, String json) {
        this.type = type;
        this.json = json;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
