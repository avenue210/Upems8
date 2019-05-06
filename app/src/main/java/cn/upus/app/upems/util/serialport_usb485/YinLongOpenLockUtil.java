package cn.upus.app.upems.util.serialport_usb485;

/**
 * 银龙 锁控板协议
 */
public class YinLongOpenLockUtil {

    /**
     * 查询
     *
     * @param boardIndex
     * @return
     */
    public static byte[] queryBox(int boardIndex) {
        byte[] to_send = new byte[11];
        to_send[0] = (byte) 0x10;
        to_send[1] = (byte) 0x02;
        to_send[2] = (byte) 0x51;
        to_send[3] = (byte) 0x53;
        to_send[4] = (byte) 0x02;
        to_send[5] = (byte) 0x00;
        to_send[6] = (byte) boardIndex;
        to_send[7] = (byte) 0x12;
        to_send[8] = (byte) (to_send[2] ^ to_send[3] ^ to_send[4] ^ to_send[5] ^ to_send[6] ^ to_send[7]);
        to_send[9] = (byte) 0x10;
        to_send[10] = (byte) 0x03;
        return to_send;
    }

    /**
     * 开锁
     *
     * @param boardIndex
     * @param lockIndex
     * @return
     */
    public static byte[] openLock(int boardIndex, int lockIndex) {
        byte[] to_send = new byte[11];
        to_send[0] = (byte) 0x10;
        to_send[1] = (byte) 0x02;
        to_send[2] = (byte) 0x57;
        to_send[3] = (byte) 0x4f;
        to_send[4] = (byte) 0x02;
        to_send[5] = (byte) 0x00;
        to_send[6] = (byte) boardIndex;
        to_send[7] = (byte) lockIndex;
        to_send[8] = (byte) (to_send[2] ^ to_send[3] ^ to_send[4] ^ to_send[5] ^ to_send[6] ^ to_send[7]);
        to_send[9] = (byte) 0x10;
        to_send[10] = (byte) 0x03;

        byte lockNo = to_send[7];
        byte validationCode = to_send[8];

        if (lockNo == 0x10) {
            byte[] to_send1 = new byte[12];
            to_send1[0] = (byte) 0x10;
            to_send1[1] = (byte) 0x02;
            to_send1[2] = (byte) 0x57;
            to_send1[3] = (byte) 0x4f;
            to_send1[4] = (byte) 0x02;
            to_send1[5] = (byte) 0x00;
            to_send1[6] = (byte) boardIndex;
            to_send1[7] = (byte) 0x10;
            to_send1[8] = (byte) 0x10;
            to_send1[9] = (byte) (to_send[2] ^ to_send[3] ^ to_send[4] ^ to_send[5] ^ to_send[6] ^ to_send[7]);
            to_send1[10] = (byte) 0x10;
            to_send1[11] = (byte) 0x03;
            return to_send1;
        } else if (validationCode == 0x10) {
            byte[] to_send1 = new byte[12];
            to_send1[0] = (byte) 0x10;
            to_send1[1] = (byte) 0x02;
            to_send1[2] = (byte) 0x57;
            to_send1[3] = (byte) 0x4f;
            to_send1[4] = (byte) 0x02;
            to_send1[5] = (byte) 0x00;
            to_send1[6] = (byte) boardIndex;
            to_send1[7] = (byte) lockIndex;
            to_send1[8] = (byte) 0x10;
            to_send1[9] = (byte) 0x10;
            to_send1[10] = (byte) 0x10;
            to_send1[11] = (byte) 0x03;
            return to_send1;
        } else {
            return to_send;
        }
    }
}
