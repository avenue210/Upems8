package cn.upus.app.upems.util.serialport_usb485;


import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

import com.blankj.utilcode.util.LogUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 获取硬件唯一ID值
 */
public class DeviceUtils {

    private Context mContext;

    public DeviceUtils(Context context) {
        this.mContext = context;
    }

    /**
     * Pseudo-Unique ID, 这个在任何Android手机中都有效
     * 有一些特殊的情况，一些如平板电脑的设置没有通话功能，或者你不愿加入READ_PHONE_STATE许可。而你仍然想获得唯
     * 一序列号之类的东西。这时你可以通过取出ROM版本、制造商、CPU型号、以及其他硬件信息来实现这一点。这样计算出
     * 来的ID不是唯一的（因为如果两个手机应用了同样的硬件以及Rom 镜像）。但应当明白的是，出现类似情况的可能性基
     * 本可以忽略。大多数的Build成员都是字符串形式的，我们只取他们的长度信息。我们取到13个数字，并在前面加上“35
     * ”。这样这个ID看起来就和15位IMEI一样了。
     * <p>
     * android.os.Build.BOARD：获取设备基板名称
     * android.os.Build.BOOTLOADER:获取设备引导程序版本号
     * android.os.Build.BRAND：获取设备品牌
     * android.os.Build.CPU_ABI：获取设备指令集名称（CPU的类型）
     * android.os.Build.CPU_ABI2：获取第二个指令集名称
     * android.os.Build.DEVICE：获取设备驱动名称
     * android.os.Build.DISPLAY：获取设备显示的版本包（在系统设置中显示为版本号）和ID一样
     * android.os.Build.FINGERPRINT：设备的唯一标识。由设备的多个信息拼接合成。
     * android.os.Build.HARDWARE：设备硬件名称,一般和基板名称一样（BOARD）
     * android.os.Build.HOST：设备主机地址
     * android.os.Build.ID:设备版本号。
     * android.os.Build.MODEL ：获取手机的型号 设备名称。
     * android.os.Build.MANUFACTURER:获取设备制造商
     * android:os.Build.PRODUCT：整个产品的名称
     * android:os.Build.RADIO：无线电固件版本号，通常是不可用的 显示unknown
     * android.os.Build.TAGS：设备标签。如release-keys 或测试的 test-keys
     * android.os.Build.TIME：时间
     * android.os.Build.TYPE:设备版本类型  主要为"user" 或"eng".
     * android.os.Build.USER:设备用户名 基本上都为android-build
     * android.os.Build.VERSION.RELEASE：获取系统版本字符串。如4.1.2 或2.2 或2.3等
     * android.os.Build.VERSION.CODENAME：设备当前的系统开发代号，一般使用REL代替
     * android.os.Build.VERSION.INCREMENTAL：系统源代码控制值，一个数字或者git hash值
     * android.os.Build.VERSION.SDK：系统的API级别 一般使用下面大的SDK_INT 来查看
     * android.os.Build.VERSION.SDK_INT：系统的API级别 数字表示
     *
     * @return PesudoUniqueID
     */
    public static String getPesudoUniqueID() {
        return "35" +
                Build.BOARD.length() % 10 +
                Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 +
                Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 +
                Build.HOST.length() % 10 +
                Build.ID.length() % 10 +
                Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 +
                Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 +
                Build.TYPE.length() % 10 +
                Build.USER.length() % 10;
    }

    /**
     * The Android ID
     * 通常被认为不可信，因为它有时为null。开发文档中说明了：这个ID会改变如果进行了出厂设置。并且，如果某个
     * Andorid手机被Root过的话，这个ID也可以被任意改变。无需任何许可。
     *
     * @return AndroidID
     */
    public String getAndroidID() {
        @SuppressLint("HardwareIds") String m_szAndroidID = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        return m_szAndroidID;
    }

    /**
     * The WLAN MAC Address string
     * 是另一个唯一ID。但是你需要为你的工程加入android.permission.ACCESS_WIFI_STATE 权限，否则这个地址会为
     * null。Returns: 00:11:22:33:44:55 (这不是一个真实的地址。而且这个地址能轻易地被伪造。).WLan不必打开，
     * 就可读取些值。
     *
     * @return m_szWLANMAC
     */
    public String getWLANMACAddress() {
        WifiManager wm = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wm != null;
        @SuppressLint("HardwareIds") String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
        return m_szWLANMAC;
    }

    /**
     * 只在有蓝牙的设备上运行。并且要加入android.permission.BLUETOOTH 权限.Returns: 43:25:78:50:93:38 .
     * 蓝牙没有必要打开，也能读取。
     *
     * @return m_szBTMAC
     */
    public static String getBTMACAddress(Context context) {
        String macAddress = Settings.Secure.getString(context.getContentResolver(), "bluetooth_address");
        return macAddress;

        /*BluetoothAdapter m_BluetoothAdapter = null;
        m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        @SuppressLint("HardwareIds") String m_szBTMAC = m_BluetoothAdapter.getAddress();
        return m_szBTMAC;*/
    }

    /**
     * Combined Device ID
     * 综上所述，我们一共有五种方式取得设备的唯一标识。它们中的一些可能会返回null，或者由于硬件缺失、权限问题等
     * 获取失败。但你总能获得至少一个能用。所以，最好的方法就是通过拼接，或者拼接后的计算出的MD5值来产生一个结果。
     * 通过算法，可产生32位的16进制数据:9DDDF85AFF0A87974CE4541BD94D5F55
     *
     * @return
     */
    public String getUniqueID(Context context) {
        String m_szLongID = getPesudoUniqueID() + getWLANMACAddress() + getBTMACAddress(context);
        // compute md5
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert m != null;
        m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
        byte p_md5Data[] = m.digest();
        StringBuilder m_szUniqueID = new StringBuilder();
        for (byte aP_md5Data : p_md5Data) {
            int b = (0xFF & aP_md5Data);
            if (b <= 0xF) {
                m_szUniqueID.append("0");
            }
            m_szUniqueID.append(Integer.toHexString(b));
        }   // hex string to uppercase
        m_szUniqueID = new StringBuilder(m_szUniqueID.toString().toUpperCase());
        return m_szUniqueID.toString();
    }

    public static String getCpuInfo() {
        String macSerial = null;
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat cat /proc/cpuinfo");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }

    public static String getMac() {
        String macSerial = null;
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }


    /**
     * getSerialNumber
     *
     * @return result is same to getSerialNumber1()
     */
    @SuppressLint("HardwareIds")
    public static String getSerialNumber() {
        String serial = null;
        try {
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

}
