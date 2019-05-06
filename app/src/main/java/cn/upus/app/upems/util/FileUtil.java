package cn.upus.app.upems.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.blankj.utilcode.util.LogUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Created by computer on 2018-03-28.
 */

public class FileUtil {

    /**
     * 保存文件的路径
     */
    public final static String saveFileDir = Environment.getExternalStorageDirectory().getPath() + "/download/";

    public final static String imageFileDir = Environment.getExternalStorageDirectory().getPath() + "/DCIM/";

    /**
     * 根据长度截取文件并返回字节数组
     *
     * @param from 复制起始点
     * @param to   复制终点
     * @param file 复制的文件
     * @return 截取后的文件
     */
    public static byte[] copyFileToByte(long from, long to, File file) {
        if (to == 0) {
            to = file.length();
        }
        if (from > file.length()) {
            throw new IllegalArgumentException("from is over size of the file:" + file.getPath());
        }
        long length = to - from;
        byte[] result = new byte[(int) length];
        RandomAccessFile accessFile = null;
        try {
            accessFile = new RandomAccessFile(file, "rw");
            accessFile.seek(from);
            int readSize = accessFile.read(result);
            if (readSize == -1) {
                return null;
            } else if (readSize == length) {
                return result;
            } else {
                byte[] tmpByte = new byte[readSize];
                System.arraycopy(result, 0, tmpByte, 0, readSize);
                return tmpByte;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (accessFile != null) {
                try {
                    accessFile.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 根据长度截取文件并返回字节数组
     *
     * @param from     复制起始点
     * @param to       复制终点
     * @param filePath 复制的文件的地址
     * @return 截取后的文件地址
     */
    public static String copyFile(long from, long to, String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return copyFile(from, to, file);
        }
        return null;
    }

    /**
     * 根据长度截取文件并返回字节数组
     *
     * @param from 复制起始点
     * @param to   复制终点
     * @param file 复制的文件
     * @return 截取后的文件地址
     */
    public static String copyFile(long from, long to, File file) {
        byte[] result = copyFileToByte(from, to, file);
        if (result != null) {
            String name = file.getPath().substring(file.getPath().lastIndexOf("/") + 1, file.getPath().length());//文件名
            mkDirNotExists(saveFileDir);
            return getFile(result, saveFileDir, name).getPath();
        }
        return null;
    }

    /**
     * 检测下载文件夹是否存在
     *
     * @param dir
     * @return
     */
    public static boolean mkDirNotExists(String dir) {
        File file = new File(dir);
        return file.exists() || file.mkdirs();
    }

    /**
     * 检测 文件是否存在
     *
     * @param str
     * @return
     */
    public static boolean fileIsExists(String str) {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String sdpath = saveFileDir + str;
                File f = new File(sdpath);
                if (!f.exists()) {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 根据byte数组，生成文件
     *
     * @param bfile
     * @param filePath
     * @param fileName
     * @return
     */
    public static File getFile(byte[] bfile, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory()) {//判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath + "\\" + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return file;
    }


    /**
     * 获取文件后缀名
     *
     * @param fileName
     * @return 文件后缀名
     */
    public static String getFileType(String fileName) {
        if (fileName != null) {
            int typeIndex = fileName.lastIndexOf(".");
            if (typeIndex != -1) {
                String fileType = fileName.substring(typeIndex + 1).toLowerCase();
                return fileType;
            }
        }
        return "";
    }

    /**
     * 根据后缀名判断是否是图片文件
     *
     * @param type
     * @return 是否是图片结果true or false
     */
    public static boolean isImage(String type) {
        if (type != null
                && (type.equals("jpg") || type.equals("gif")
                || type.equals("png") || type.equals("jpeg")
                || type.equals("bmp") || type.equals("wbmp")
                || type.equals("ico") || type.equals("jpe"))) {
            return true;
        }
        return false;
    }

    /**
     * 根据后缀名判断是否是媒体文件
     *
     * @param type
     * @return 是否是图片结果true or false
     */
    public static boolean isMedia(String type) {
        if (type != null
                && (type.equals("wav") || type.equals("mp3")
                || type.equals("aif") || type.equals("au")
                || type.equals("ram") || type.equals("wma")
                || type.equals("mmf") || type.equals("amr")
                || type.equals("aac") || type.equals("flac"))) {
            return true;
        }
        return false;
    }

    /**
     * 将Bitmap保存到SD卡 并出入到系统图库
     *
     * @param context
     * @param bmp
     */
    public static String saveImageToGallery(Context context, Bitmap bmp) {
        //首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "Mobileoffice");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
            LogUtils.e("Bitmap图片保存路径", file.getAbsolutePath() + " 文件名称:" + fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //最后通知图库更新
        //context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(file.getAbsolutePath())));
        return file.getAbsolutePath();
    }

    /**
     * 将Bitmap保存到SD卡 并出入到系统图库
     *
     * @param context
     * @param bmp
     * @param name
     * @return
     */
    public static String saveImageToGallery(Context context, Bitmap bmp, String name) {
        //首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "Mobileoffice");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = name + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
            LogUtils.e("Bitmap图片保存路径", file.getAbsolutePath() + " 文件名称:" + fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //最后通知图库更新
        //context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(file.getAbsolutePath())));
        return file.getAbsolutePath();
    }

    /**
     * 将字节数组转换为ImageView可调用的Bitmap对象
     * @param bytes
     * @param opts
     * @return
     */
    public static Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts) {
        if (bytes != null)
            if (opts != null) {
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
            } else {
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        return null;
    }

    /**
     * 将图片内容解析成字节数组
     * @param inStream
     * @return
     * @throws Exception
     */
    public static byte[] readStream(InputStream inStream) throws Exception {
        byte[] buffer = new byte[1024];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;
    }
}
