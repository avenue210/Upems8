package cn.upus.app.upems.util.serialport_usb485;

import android.util.Log;

import com.blankj.utilcode.util.LogUtils;

/**
 * 说明：
 * CMD：		命令/应答类型
 * P1，P2，P3：命令参数
 * Q1，Q2，Q3：应答参数，
 * Q3多用于返回操作的有效性信息，此时可有如下取值：
 * #define ACK_SUCCESS  	        0x00	//操作成功
 * #define ACK_FAIL	  		        0x01	//操作失败
 * #define ACK_GETFP_OK	            0x02	//指纹采集成功
 * #define ACK_FULL	  	            0x04	//指纹数据库已满
 * #define ACK_NOUSER   	        0x05	//无此用户
 * #define ACK_USER_OCCUPIED		0x06	//用户已存在
 * #define ACK_FINGER_OCCUPIED 	    0x07 	//指纹已存在
 * #define ACK_TIMEOUT  	        0x08	//采集超时
 * CHK：		校验和，为第2字节到第6字节的异或值
 */
public class ZhiWenUtil {

    /**
     * 异或运算
     *
     * @param datas
     * @return
     */
    private static byte getXor(byte[] datas) {
        byte temp = datas[0];
        for (int i = 1; i < datas.length; i++) {
            temp ^= datas[i];
        }
        return temp;
    }

    /**
     * 1 修改模块序列号（命令/应答均为8字节）
     * 命令	0xF5	0x08	新序列号(位23-16	新序列号(位15-8)	新序列号(位7-0)	0	CHK	0xF5
     * 应答	0xF5	0x08	旧序列号(位23-16	旧序列号(位15-8)	旧序列号(位7-0)	0	CHK	0xF5
     *
     * @param var
     * @return
     */
    public static byte[] w_1(String var) {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x08;
        to_send[2] = (byte) Integer.parseInt(var.substring(0, 2), 16);//新序列号(位23-16)
        to_send[3] = (byte) Integer.parseInt(var.substring(2, 4), 16);//新序列号(位15-8)
        to_send[4] = (byte) Integer.parseInt(var.substring(4, 6), 16);//新序列号(位7-0)
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        return to_send;
    }

    /**
     * 2 读取模块内部序列号（命令/应答均为8字节）
     * 命令	0xF5	0x2A	0	  0	0	0	CHK	0xF5
     * 应答	0xF5	0x2A	序列号(位12-16)	序列号(位15-8)	序列号(位7-0)	0	CHK	0xF5
     *
     * @return
     */
    public static byte[] w_2() {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x2A;
        to_send[2] = (byte) 0;
        to_send[3] = (byte) 0;
        to_send[4] = (byte) 0;
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        return to_send;
    }

    /**
     * 3 使模块进入休眠状态（命令/应答均为8字节）
     * 命令	0xF5	0x2C	0	0	0	0	CHK	0xF5
     * 应答	0xF5	0x2C	0	0	0	0	CHK	0xF5
     *
     * @return
     */
    public static byte[] w_3() {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x2C;
        to_send[2] = (byte) 0;
        to_send[3] = (byte) 0;
        to_send[4] = (byte) 0;
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        return to_send;
    }

    /**
     * 4 设置/读取指纹添加模式（命令/应答均为8字节）
     * 命令	0xF5	0x2D	0	Byte5=0：  0：允许重复1：禁止重复 Byte5=1： 0	0：设置新的添加模式 1：读取当前添加模式	 0	CHK	0xF5
     * 应答	0xF5	0x2D	0	当前添加模式	ACK_SUCCUSS ACK_FAIL	0	CHK	0xF5
     *
     * @return
     */
    public static byte[] w_4() {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x2D;
        to_send[2] = (byte) 0;
        to_send[3] = (byte) 0;
        to_send[4] = (byte) 0;//0：设置新的添加模式 1：读取当前添加模式
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        return to_send;
    }

    /**
     * 5 添加指纹1（命令/应答均为8字节）
     * 命令	0xF5	0x01	用户号（高8位）	用户号（低8位）	用户权限(1-3)	0	CHK	0xF5
     * 应答1	0xF5	0x01	0	0	ACK_GETFP_OK  ACK_FULL ACK_USER_OCCUPIED ACK_TIMEOUT	0	CHK	0xF5
     * 应答2	0xF5	0x01	0	0	ACK_SUCCESS  ACK_FAIL ACK_FINGER_OCCUPIED	0	CHK	0xF5
     *
     * @return
     */
    public static byte[] w_5(String var) {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x01;
        to_send[2] = (byte) Integer.parseInt(var.substring(0, 2), 16);//用户号（高8位）
        to_send[3] = (byte) Integer.parseInt(var.substring(2, 4), 16);//用户号（低8位）
        to_send[4] = (byte) 1;//用户权限(1-3)
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        Log.e("串口命令", "添加指纹1");
        return to_send;
    }

    public static byte[] w_5(String var, int type) {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x01;
        to_send[2] = (byte) Integer.parseInt(var.substring(0, 2), 16);//用户号（高8位）
        to_send[3] = (byte) Integer.parseInt(var.substring(2, 4), 16);//用户号（低8位）
        to_send[4] = (byte) type;//用户权限(1-3)
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        Log.e("串口命令", "添加指纹1");
        return to_send;
    }

    /**
     * 5 添加指纹2（命令/应答均为8字节）
     * 应答1	0xF5	0x02	0	0	ACK_GETFP_OK  ACK_TIMEOUT	0	CHK	0xF5
     * 应答2	0xF5	0x02	0	0	ACK_SUCCESS ACK_FAIL	0	CHK	0xF5
     *
     * @param var
     * @return
     */
    public static byte[] w_6(String var) {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x02;
        to_send[2] = (byte) Integer.parseInt(var.substring(0, 2), 16);//用户号（高8位）
        to_send[3] = (byte) Integer.parseInt(var.substring(2, 4), 16);//用户号（低8位）
        to_send[4] = (byte) 1;//用户权限(1-3)
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        Log.e("串口命令", "添加指纹2");
        return to_send;
    }

    public static byte[] w_6(String var, int type) {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x02;
        to_send[2] = (byte) Integer.parseInt(var.substring(0, 2), 16);//用户号（高8位）
        to_send[3] = (byte) Integer.parseInt(var.substring(2, 4), 16);//用户号（低8位）
        to_send[4] = (byte) type;//用户权限(1-3)
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        Log.e("串口命令", "添加指纹2");
        return to_send;
    }

    /**
     * 5 添加指纹3（命令/应答均为8字节）
     * 应答2仅在应答1参数Q3为ACK_GETFP_OK时返回。3次命令中用户号与用户权限应为相同值
     * 如果第3次发的命令为0x06，模块会将注册成功的特征值返回给主设备而不写入模块数据库
     * 应答1	0xF5	0x03	0	0	ACK_GETFP_OK ACK_TIMEOUT	0	CHK	0xF5
     * 应答2	0xF5	0x03	0	0	ACK_SUCCESS ACK_FAIL	0	CHK	0xF5
     *
     * @param var
     * @return
     */
    public static byte[] w_7(String var) {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x03;
        to_send[2] = (byte) Integer.parseInt(var.substring(0, 2), 16);//用户号（高8位）
        to_send[3] = (byte) Integer.parseInt(var.substring(2, 4), 16);//用户号（低8位）
        to_send[4] = (byte) 1;//用户权限(1-3)
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        Log.e("串口命令", "添加指纹3");
        return to_send;
    }

    public static byte[] w_7(String var, int type) {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x03;
        to_send[2] = (byte) Integer.parseInt(var.substring(0, 2), 16);//用户号（高8位）
        to_send[3] = (byte) Integer.parseInt(var.substring(2, 4), 16);//用户号（低8位）
        to_send[4] = (byte) type;//用户权限(1-3)
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        Log.e("串口命令", "添加指纹3");
        return to_send;
    }

    /**
     * 6 删除指定用户
     * 命令	0xF5	0x04	用户号（高8位）	用户号（低8位）	0	0	CHK	0xF5
     * 应答	0xF5	0x04	0	0	ACK_SUCCESS ACK_FAIL	0	CHK	0xF5
     *
     * @param var
     * @return
     */
    public static byte[] w_8(String var) {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x04;
        to_send[2] = (byte) Integer.parseInt(var.substring(0, 2), 16);//用户号（高8位）
        to_send[3] = (byte) Integer.parseInt(var.substring(2, 4), 16);//用户号（低8位）
        to_send[4] = (byte) 0;
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        return to_send;
    }

    /**
     * 7删除所有用户（命令/应答均为8字节）
     * 命令	0xF5	0x05	0	0	0：删除全部用户 1/2/3：删除权限为1/2/3的全部用户	0	CHK	0xF5
     * 应答	0xF5	0x05	0	0	ACK_SUCCESS  ACK_FAIL	0	CHK	0xF5
     *
     * @return
     */
    public static byte[] w_9() {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x05;
        to_send[2] = (byte) 0;
        to_send[3] = (byte) 0;
        to_send[4] = (byte) 0;//0：删除全部用户 1/2/3：删除权限为1/2/3的全部用户
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        return to_send;
    }

    public static byte[] w_9(int type) {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x05;
        to_send[2] = (byte) 0;
        to_send[3] = (byte) 0;
        to_send[4] = (byte) type;//0：删除全部用户 1/2/3：删除权限为1/2/3的全部用户
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        return to_send;
    }

    /**
     * 8 取用户总数（命令/应答均为8字节）
     * 命令	0xF5	0x09	0	0	0：取用户总数 0xFF：取指纹容量	0	CHK	0xF5
     * 应答	0xF5	0x09	用户数/指纹容量（高8位）	用户数/指纹容量（低8位）	ACK_SUCCESS  ACK_FAIL 0xFF（如果命令为取容量）	0	CHK	0xF5
     *
     * @return
     */
    public static byte[] w_10() {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x09;
        to_send[2] = (byte) 0;
        to_send[3] = (byte) 0;
        to_send[4] = (byte) 0;//0：取用户总数 0xFF：取指纹容量
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        return to_send;
    }

    /**
     * 取指纹容量
     *
     * @return
     */
    public static byte[] w_10B() {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x09;
        to_send[2] = (byte) 0;
        to_send[3] = (byte) 0;
        to_send[4] = (byte) 0xFF;//0：取用户总数 0xFF：取指纹容量
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        return to_send;
    }

    /**
     * 9 比对1：1（命令/应答均为8字节）
     * 应答1	0xF5	0x0B	0	0	ACK_GETFP_OK  ACK_TIMEOUT	0	CHK	0xF5
     * 应答2	0xF5	0x0B	0	0	ACK_SUCCESS  ACK_FAIL	0	CHK	0xF5
     *
     * @return
     */
    public static byte[] w_11(String var) {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x0B;
        to_send[2] = (byte) Integer.parseInt(var.substring(0, 2), 16);//用户号（高8位）
        to_send[3] = (byte) Integer.parseInt(var.substring(2, 4), 16);//用户号（低8位）
        to_send[4] = (byte) 0;
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        return to_send;
    }

    /**
     * 10 比对1：N（命令/应答均为8字节）
     * 命令	0xF5	0x0C	0	0	0	0	CHK	0xF5
     * 应答1	0xF5	0x0C	0	0	ACK_GETFP_OK  ACK_TIMEOUT	0	CHK	0xF5
     *
     * @return
     */
    public static byte[] w_12() {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x0C;
        to_send[2] = (byte) 0;
        to_send[3] = (byte) 0;
        to_send[4] = (byte) 0;
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        LogUtils.e("串口命令", "1 ： N 验证用户是否存在");
        return to_send;
    }

    /**
     * 11 取用户权限（命令/应答均为8字节）
     * 命令	0xF5	0x0A	用户号（高8位）	用户号（低8位）	0	0	CHK	0xF5
     * 应答	0xF5	0x0A	0	0	用户权限(1/2/3) ACK_NOUSER	0	CHK	0xF5
     *
     * @param var
     * @return
     */
    public static byte[] w_13(String var) {
        byte[] to_send = new byte[8];
        to_send[0] = (byte) 0xF5;
        to_send[1] = (byte) 0x0A;
        to_send[2] = (byte) Integer.parseInt(var.substring(0, 2), 16);//用户号（高8位）
        to_send[3] = (byte) Integer.parseInt(var.substring(2, 4), 16);//用户号（低8位）
        to_send[4] = (byte) 0;
        to_send[5] = (byte) 0;
        to_send[6] = getXor(new byte[]{to_send[1], to_send[2], to_send[3], to_send[4], to_send[5]});
        to_send[7] = (byte) 0xF5;
        return to_send;
    }

}
