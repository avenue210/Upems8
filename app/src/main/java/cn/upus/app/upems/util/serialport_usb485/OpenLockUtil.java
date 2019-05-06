package cn.upus.app.upems.util.serialport_usb485;

/**
 * 普通开锁指令
 */
public class OpenLockUtil {

    /**
     * 开锁
     *
     * @param boardIndex 板位置
     * @param lockIndex  锁位置
     */
    public static byte[] openLock(int boardIndex, int lockIndex) {
        byte[] to_send = new byte[5];
        to_send[0] = (byte) 0x8A;
        to_send[1] = (byte) boardIndex;
        to_send[2] = (byte) lockIndex;
        to_send[3] = (byte) 0x11;
        to_send[4] = (byte) (to_send[0] ^ to_send[1] ^ to_send[2] ^ to_send[3]);
        return to_send;
    }

    /**
     * 全部开锁
     *
     * @param boardIndex 板位置
     */
    public static byte[] openLockAll(int boardIndex) {
        byte[] to_send = new byte[5];
        to_send[0] = (byte) 0x9d;
        to_send[1] = (byte) boardIndex;
        to_send[2] = (byte) 0x02;
        to_send[3] = (byte) 0x11;
        to_send[4] = (byte) (to_send[0] ^ to_send[1] ^ to_send[2] ^ to_send[3]);
        return to_send;
    }

    /**
     * 开关灯
     *
     * @param boardIndex
     * @param lockIndex
     * @param type       0 关 1 开
     * @return
     */
    public static byte[] openD(int boardIndex, int lockIndex, int type) {
        byte[] to_send = new byte[5];
        if (type == 0) {//关
            to_send[0] = (byte) 0x9B;
        } else if (type == 1) {//开
            to_send[0] = (byte) 0x9A;
        }

        to_send[1] = (byte) boardIndex;
        to_send[2] = (byte) lockIndex;

        if (type == 0) {//关
            to_send[3] = (byte) 0x10;

        } else if (type == 1) {//开
            to_send[3] = (byte) 0x11;
        }

        to_send[4] = (byte) (to_send[0] ^ to_send[1] ^ to_send[2] ^ to_send[3]);
        return to_send;
    }

    /**
     * 读取锁状态 第三位 0X00 锁关 0X11 锁开
     *
     * @param boardIndex
     * @param lockIndex
     * @return
     */
    public static byte[] queryBox(int boardIndex, int lockIndex) {
        byte[] to_send = new byte[5];
        to_send[0] = (byte) 0x80;
        to_send[1] = (byte) boardIndex;
        to_send[2] = (byte) lockIndex;
        to_send[3] = (byte) 0x33;
        to_send[4] = (byte) (to_send[0] ^ to_send[1] ^ to_send[2] ^ to_send[3]);
        return to_send;
    }

    /**
     * 读取全部锁状态
     *
     * @param boardIndex
     * @return
     */
    public static byte[] queryBoxAll(int boardIndex) {
        byte[] to_send = new byte[5];
        to_send[0] = (byte) 0x80;
        to_send[1] = (byte) boardIndex;
        to_send[2] = (byte) 0x0;
        to_send[3] = (byte) 0x33;
        to_send[4] = (byte) (to_send[0] ^ to_send[1] ^ to_send[2] ^ to_send[3]);
        return to_send;
    }

}
