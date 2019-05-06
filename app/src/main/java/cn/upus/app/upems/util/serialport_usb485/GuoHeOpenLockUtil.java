package cn.upus.app.upems.util.serialport_usb485;

/**
 * 果核开锁协议
 */
public class GuoHeOpenLockUtil {

    /*开锁：BB（固定） 02（锁号） 0A（开锁指令） FF（固定） 0B（2+3+4） 03（固定）
     状态：BB 02 0A 01（状态） 01 0E FE ,长度：7
     状态：BB（固定） 02（锁号） 0B（状态指令） FF（固定） 0C（2+3+4） 03（固定）
     接收：BB 02 0B 01（状态） 01 0F FE ,长度：7
     BB FE 01（第一把锁地址） 02（第二把锁地址） 01（2+3+4） 03
    */

    /**
     * 开锁
     *
     * @param lockIndex
     * @return
     */
    public static byte[] openLock(int lockIndex) {
        byte[] to_send = new byte[6];
        to_send[0] = (byte) 0xBB;
        to_send[1] = (byte) lockIndex;
        to_send[2] = (byte) 0x0A;
        to_send[3] = (byte) 0xFF;
        to_send[4] = (byte) (to_send[1] + to_send[2] + to_send[3]);
        to_send[5] = (byte) 0x03;
        return to_send;
    }

    /**
     * 查询
     *
     * @param lockIndex
     * @return
     */
    public static byte[] queryBox(int lockIndex) {
        byte[] to_send = new byte[6];
        to_send[0] = (byte) 0xBB;
        to_send[1] = (byte) lockIndex;
        to_send[2] = (byte) 0x0B;
        to_send[3] = (byte) 0xFF;
        to_send[4] = (byte) (to_send[1] + to_send[2] + to_send[3]);
        to_send[5] = (byte) 0x03;
        return to_send;
    }

    /**
     * 写入指定锁号
     *
     * @param id1
     * @param id2
     * @return
     */
    public static byte[] writeLockID(int id1, int id2) {
        byte[] to_send = new byte[6];
        to_send[0] = (byte) 0xBB;
        to_send[1] = (byte) 0xFE;
        to_send[2] = (byte) id1;
        to_send[3] = (byte) id2;
        to_send[4] = (byte) (to_send[1] + to_send[2] + to_send[3]);
        to_send[5] = (byte) 0x03;
        return to_send;
    }

}
