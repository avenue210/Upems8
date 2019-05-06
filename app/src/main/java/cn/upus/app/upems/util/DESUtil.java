package cn.upus.app.upems.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;


public class DESUtil {

    public static String decrypt(String message, String key) {
        byte[] bytesrc = convertHexString(message);
        String res = null;
        try {
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

            byte[] retByte = cipher.doFinal(bytesrc);
            res = new String(retByte, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static String decrypt(String message) {
        byte[] bytesrc = convertHexString(message);
        String res = null;
        try {
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec("liscjw27".getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec("liscjw27".getBytes("UTF-8"));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

            byte[] retByte = cipher.doFinal(bytesrc);
            res = new String(retByte, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static String encrypt(String message, String key) {
        String result = "";
        try {
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] bytes = cipher.doFinal(message.getBytes("UTF-8"));
            result = toHexString(bytes).toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String encrypt(String message) {
        String result = "";
        try {
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec("liscjw27".getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec("liscjw27".getBytes("UTF-8"));
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] bytes = cipher.doFinal(message.getBytes("UTF-8"));
            result = toHexString(bytes).toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static byte[] convertHexString(String ss) {
        byte digest[] = new byte[ss.length() / 2];
        for (int i = 0; i < digest.length; i++) {
            String byteString = ss.substring(2 * i, 2 * i + 2);
            int byteValue = Integer.parseInt(byteString, 16);
            digest[i] = (byte) byteValue;
        }
        return digest;
    }

    private static String toHexString(byte b[]) {
        StringBuffer hexString = new StringBuffer();
        for (byte aB : b) {
            String plainText = Integer.toHexString(0xff & aB);
            if (plainText.length() < 2) {
                plainText = "0" + plainText;
            }
            hexString.append(plainText);
        }
        return hexString.toString();
    }

    public static void main(String[] args) throws Exception {
        System.out.println(decrypt("91AF2FCE3CB6B8CC79D8C78282BEF34B09747C98CFEDF2419EC387C992DBE9C7660D4A0CDE69C5165FD58058D130E881D564E0F82263FAD8BA2756B843D15FFCC11ADFD13611742BA8F70C5B23BDD6FC981E7CE737BCF9DF98F62118F9B9E22D2D5B538A4BAF989CD9AD9E25322EEC1E33E810C5274D865847F2C749FA93F8B115D4AA7162FD49C10F8CC3A3E3BDD6093C727718E515D60FFFF3129D3FF584E94B56A391CB9FBA4A4D86F819D4C3C46ED56CF1E5526CEB7FE485D347C511F41F8A2AE4BE782E5D12AA296956C338479268A55DC6ADF96389C712636384FD7F10ED711437D39C08945F3AB7269657FD94D981303AD43302ED953FF32568EE8D49EB60A276F1FC57A277983F5ED3688D41A398C1C75FDB3CAD957F194A7AC8F84F6F4CAB5CE558E08898322EE92CE9B939315507834A8D217EAB9035FA7E1DF8D6A6109A58B1DF4C978A56F3D9223F6E421B947E57F4C56767E0D23A069C1B40D5133C87E7555802D50B1009C3B3B0BD68A6317CE12C9C1AABC10AB097C686D64ECD272566074E96831E15D4954C6591F1C0E2731AF5258096D24B6B6A917E95AB2D487FD4DB28CAF10FA368502F905EAB80568798BFC831C96F5E0DF7D4155D5D1CEB16489487928DC47CE5EE29F3E3BA77F184D0ECB83C6A9335F3429B66490ED2C3AFB36919359DEE9A7742CF170CE4AACAA4E509844E139AE62D8F310F975B33FB088562EF56294B8A01BF7C7B437015082C586FDBC5719C694CA6667EF4964D0E07EDF559E4C77CF81FE3224080F68389E4259DFA7942F1E49C31A4F7EDF0547E2427CBC68EFB6E3BCD295232DBEFC7D552AA00B19C70D5F6AA49C2CC3887B1C3524992BD52BD2019540F362FC0C753E8F5AE686AD6904A3BA9C37E48ECF69B031CB28A5988E40B1D206130DB0F3B9DF8B2D8C1C8332F8EFBB7469291DB8605E44A7E0C673624060BABB36740E025CCCA63D2EB991C88ABDDEC38CD381B5AC0D8200B1984E313917E0C34F24A4AEE82583B266B1877983E43B0ABF7A15B37D2E943B3C25C04506B00ED8D26C1E7867AAF99051FD39F261BE23CBD13E3731563810D462133D457D187330469FCC2C121FE937B556BB37B1D6385478F2A4AB751A061878A8076EA151E12C4D66CFD7A0138E148B4244DB2E832E6D682BA72C8801EFE77FFD3EB472A52D3277F15FD7F15A12C38F813365132DE7D041F19EC80CF98DB09C074614B89BF83C03A2E74F7AE1A10DBD2380C70D2EE88294E3E6A41E07DB392B3C3501B29304D5DE80549927D18CCFEAFA325705B5ADA68DA71E487091CAA88C47E2190C1C40E7688DB2F6A347530E1479DF6017087A37A73CB3C701CED551CB04E18AF", "liscjw27"));
        String str = "{\"buyerAddress\":\"湖南邵阳洞口县洞口雪峰广场西侧厂家网商城020店铺\",\"buyerMobile\":\"\",\"buyerName\":\"张女士\",\"buyerRemark\":\"洞口店铺已取货，无需发货\",\"buyerTelephone\":\"15329685317\",\"cityCode\":\"204\",\"cityName\":\"邵阳\",\"createTime\":\"2016-04-01 18:48\",\"deliveryInfo\":[],\"detail\":[{\"orderDatailId\":130610,\"price\":\"498.00\",\"productId\":68089,\"productName\":\"爱为奇 新款笑脸真皮女手提包 撞色蛇皮纹真皮单肩包斜挎\",\"productQuantity\":1,\"productSpecifications\":\"款式:小款,颜色:黑白色\",\"sumPrice\":\"498.00\"}],\"orderCode\":\"12016040167640031\",\"orderStatus\":4,\"provinceCode\":\"14\",\"provinceName\":\"湖南\",\"regionCode\":\"1719\",\"regionName\":\"洞口县\",\"storeAddress\":\"广东东莞企石镇东莞市企石镇霞朗工业开发区 \",\"storeFullName\":\"东莞市顶好手袋皮具有限公司\",\"storeId\":1537,\"storeManageName\":\"文家涛\",\"storeName\":\"东莞市顶好手袋皮具有限公司\",\"storePhone\":\"15307690268\"}";
        System.out.println(encrypt(str, "liscjw27"));
    }
}
