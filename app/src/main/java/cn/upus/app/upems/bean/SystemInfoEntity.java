package cn.upus.app.upems.bean;

/**
 * 设备信息
 * 设备位置 depotna libna
 * 设备编号 fixno
 * 位置代码 libno
 */
public class SystemInfoEntity {

    /**
     * depotna : 上海互巴
     * depotno : 001
     * libno : 00103
     * fixna : 大配餐柜3号
     * libna : 1
     */

    private String depotna;
    private String depotno;
    private String libno;
    private String fixna;
    private String libna;
    private String mainpic;//下半部 背景图片
    private String custno;//该参数扫码注册时使用

    private String switchtoken;//尼日利亚支付
    private String tentoken;//微信
    private String alitoken;//支付宝
    private String userna;//联系人
    private String deptel;//联系电话

    public SystemInfoEntity() {
    }

    public String getDepotna() {
        return depotna;
    }

    public void setDepotna(String depotna) {
        this.depotna = depotna;
    }

    public String getDepotno() {
        return depotno;
    }

    public void setDepotno(String depotno) {
        this.depotno = depotno;
    }

    public String getLibno() {
        return libno;
    }

    public void setLibno(String libno) {
        this.libno = libno;
    }

    public String getFixna() {
        return fixna;
    }

    public void setFixna(String fixna) {
        this.fixna = fixna;
    }

    public String getLibna() {
        return libna;
    }

    public void setLibna(String libna) {
        this.libna = libna;
    }

    public String getMainpic() {
        return mainpic;
    }

    public void setMainpic(String mainpic) {
        this.mainpic = mainpic;
    }

    public String getCustno() {
        return custno;
    }

    public void setCustno(String custno) {
        this.custno = custno;
    }

    public String getSwitchtoken() {
        return switchtoken;
    }

    public void setSwitchtoken(String switchtoken) {
        this.switchtoken = switchtoken;
    }

    public String getTentoken() {
        return tentoken;
    }

    public void setTentoken(String tentoken) {
        this.tentoken = tentoken;
    }

    public String getAlitoken() {
        return alitoken;
    }

    public void setAlitoken(String alitoken) {
        this.alitoken = alitoken;
    }

    public String getUserna() {
        return userna;
    }

    public void setUserna(String userna) {
        this.userna = userna;
    }

    public String getDeptel() {
        return deptel;
    }

    public void setDeptel(String deptel) {
        this.deptel = deptel;
    }
}
