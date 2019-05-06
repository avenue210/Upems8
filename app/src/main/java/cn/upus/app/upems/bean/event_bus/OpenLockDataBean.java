package cn.upus.app.upems.bean.event_bus;

import java.util.List;

/**
 * 开锁数据
 */
public class OpenLockDataBean {

    /**
     * 0 一个位置 1 多个位置 3 全部位置
     */
    private int position;

    /**
     * 指令 类型 0 开锁 / 1 查询单个 / 2 开灯 / 3 关灯 / 4 查询全部 / 5 全部开锁
     */
    private int type;

    private int boardIndex;
    private int lockIndex;

    private List<Locks> locks;

    public OpenLockDataBean() {

    }

    /**
     * @param position   0 一个位置 1 多个位置 3 全部位置
     * @param type       指令 类型 0 开锁 / 1 查询单个 / 2 开灯 / 3 关灯 / 4 查询全部 / 5 全部开锁
     * @param boardIndex 板号
     * @param lockIndex  锁号
     * @param locks      多个板号锁号
     */
    public OpenLockDataBean(int position, int type, int boardIndex, int lockIndex, List<Locks> locks) {
        this.position = position;
        this.type = type;
        this.boardIndex = boardIndex;
        this.lockIndex = lockIndex;
        this.locks = locks;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getBoardIndex() {
        return boardIndex;
    }

    public void setBoardIndex(int boardIndex) {
        this.boardIndex = boardIndex;
    }

    public int getLockIndex() {
        return lockIndex;
    }

    public void setLockIndex(int lockIndex) {
        this.lockIndex = lockIndex;
    }

    public List<Locks> getLocks() {
        return locks;
    }

    public void setLocks(List<Locks> locks) {
        this.locks = locks;
    }

    @Override
    public String toString() {
        return "OpenLockDataBean{" +
                "position=" + position +
                ", type=" + type +
                ", boardIndex=" + boardIndex +
                ", lockIndex=" + lockIndex +
                ", locks=" + locks +
                '}';
    }

    public static class Locks {
        private int boardIndex;
        private int lockIndex;

        public int getBoardIndex() {
            return boardIndex;
        }

        public void setBoardIndex(int boardIndex) {
            this.boardIndex = boardIndex;
        }

        public int getLockIndex() {
            return lockIndex;
        }

        public void setLockIndex(int lockIndex) {
            this.lockIndex = lockIndex;
        }

        @Override
        public String toString() {
            return "Locks{" +
                    "boardIndex=" + boardIndex +
                    ", lockIndex=" + lockIndex +
                    '}';
        }
    }

}
