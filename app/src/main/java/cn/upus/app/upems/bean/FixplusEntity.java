package cn.upus.app.upems.bean;

/**
 * 定时任务
 */
public class FixplusEntity {

    /**
     * statetime : 19:20
     * pluskindna : 风扇
     * stateno : 0
     * pluskind : 1
     * plusno : 2
     * boardno : 0
     */

    private String statetime; //时间
    private String endtime;//结束时间
    private String pluskindna; //子设备名称
    private String stateno;//开关 0 关 1 开
    private String pluskind;//子设备号
    private String plusno;//锁号
    private String boardno;//板号
    private String plusna;//设备名称
    private String fileurl;//图片地址


    public String getStatetime() {
        return statetime;
    }

    public void setStatetime(String statetime) {
        this.statetime = statetime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public String getPluskindna() {
        return pluskindna;
    }

    public void setPluskindna(String pluskindna) {
        this.pluskindna = pluskindna;
    }

    public String getStateno() {
        return stateno;
    }

    public void setStateno(String stateno) {
        this.stateno = stateno;
    }

    public String getPluskind() {
        return pluskind;
    }

    public void setPluskind(String pluskind) {
        this.pluskind = pluskind;
    }

    public String getPlusno() {
        return plusno;
    }

    public void setPlusno(String plusno) {
        this.plusno = plusno;
    }

    public String getBoardno() {
        return boardno;
    }

    public void setBoardno(String boardno) {
        this.boardno = boardno;
    }

    public String getPlusna() {
        return plusna;
    }

    public void setPlusna(String plusna) {
        this.plusna = plusna;
    }

    public String getFileurl() {
        return fileurl;
    }

    public void setFileurl(String fileurl) {
        this.fileurl = fileurl;
    }

    @Override
    public String toString() {
        return "FixplusEntity{" +
                "statetime='" + statetime + '\'' +
                ", endtime='" + endtime + '\'' +
                ", pluskindna='" + pluskindna + '\'' +
                ", stateno='" + stateno + '\'' +
                ", pluskind='" + pluskind + '\'' +
                ", plusno='" + plusno + '\'' +
                ", boardno='" + boardno + '\'' +
                ", plusna='" + plusna + '\'' +
                ", fileurl='" + fileurl + '\'' +
                '}';
    }
}
