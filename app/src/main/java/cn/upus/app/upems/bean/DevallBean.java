package cn.upus.app.upems.bean;

/**
 * 设备格子信息
 */
public class DevallBean {


    /**
     * lockno : 1
     * posno : A01
     * shelfno : A01
     * boardno : 1
     */

    private String lockno;
    private String posno;
    private String shelfno;
    private String boardno;

    private String bordercnt;//板子数量
    private String type;//开关状态 0 / 1

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setBordercnt(String bordercnt) {
        this.bordercnt = bordercnt;
    }

    public String getBordercnt() {
        return bordercnt;
    }

    public String getLockno() {
        return lockno;
    }

    public void setLockno(String lockno) {
        this.lockno = lockno;
    }

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

    public String getBoardno() {
        return boardno;
    }

    public void setBoardno(String boardno) {
        this.boardno = boardno;
    }

    @Override
    public String toString() {
        return "DevallBean{" +
                "lockno='" + lockno + '\'' +
                ", posno='" + posno + '\'' +
                ", shelfno='" + shelfno + '\'' +
                ", boardno='" + boardno + '\'' +
                ", bordercnt='" + bordercnt + '\'' +
                '}';
    }
}
