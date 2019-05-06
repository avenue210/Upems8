package cn.upus.app.upems.bean;

/**
 * 取件参数
 */
public class PartParameterEntity {

    /**
     * expno : 34871513697447
     * lockno : 1
     * shelfno : A01
     * boardno : 1
     * action : 0
     */

    private String expno;
    private String lockno;
    private String shelfno;
    private String boardno;
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getExpno() {
        return expno;
    }

    public void setExpno(String expno) {
        this.expno = expno;
    }

    public String getLockno() {
        return lockno;
    }

    public void setLockno(String lockno) {
        this.lockno = lockno;
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
}