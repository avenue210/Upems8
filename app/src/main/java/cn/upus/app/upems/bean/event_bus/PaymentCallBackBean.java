package cn.upus.app.upems.bean.event_bus;

/**
 * 支付成功信息
 */
public class PaymentCallBackBean {

    private int type;

    public PaymentCallBackBean(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
