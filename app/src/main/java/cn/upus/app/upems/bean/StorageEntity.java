package cn.upus.app.upems.bean;

/**
 * 配送 存 参数
 */
public class StorageEntity {

    private String posno;//货位号
    private String shelfno;//柜号
    private String barno;//单号
    private String tel;//手机号
    private String price;//金额
    private String expno;//本地随机生成的单号

    private String boardno;//版号
    private String lockno;//锁号

    private boolean lockOk;

    private String kindna;//格子类型名字
    private String kindno;//格子大中小类型

    public String getPosno() {
        return posno;
    }

    public void setPosno(String posno) {
        this.posno = posno;
    }

    public String getShelfno() {
        return shelfno;
    }

    public void setShelfno(String shelfno) {
        this.shelfno = shelfno;
    }

    public String getBarno() {
        return barno;
    }

    public void setBarno(String barno) {
        this.barno = barno;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getExpno() {
        return expno;
    }

    public void setExpno(String expno) {
        this.expno = expno;
    }

    public String getBoardno() {
        return boardno;
    }

    public void setBoardno(String boardno) {
        this.boardno = boardno;
    }

    public String getLockno() {
        return lockno;
    }

    public void setLockno(String lockno) {
        this.lockno = lockno;
    }

    public boolean isLockOk() {
        return lockOk;
    }

    public void setLockOk(boolean lockOk) {
        this.lockOk = lockOk;
    }

    public String getKindna() {
        return kindna;
    }

    public void setKindna(String kindna) {
        this.kindna = kindna;
    }

    public String getKindno() {
        return kindno;
    }

    public void setKindno(String kindno) {
        this.kindno = kindno;
    }

    @Override
    public String toString() {
        return "StorageEntity{" +
                "posno='" + posno + '\'' +
                ", shelfno='" + shelfno + '\'' +
                ", barno='" + barno + '\'' +
                ", tel='" + tel + '\'' +
                ", price='" + price + '\'' +
                ", expno='" + expno + '\'' +
                ", boardno='" + boardno + '\'' +
                ", lockno='" + lockno + '\'' +
                ", lockOk=" + lockOk +
                ", kindna='" + kindna + '\'' +
                ", kindno='" + kindno + '\'' +
                '}';
    }
}
