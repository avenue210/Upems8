package cn.upus.app.upems.util;

import android.os.Environment;
import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * 文件 写入 读取
 */
public class IO {

    /*new Thread(() -> {
            IO io = new IO();
            File file = new File(io.getFilePath() + io.fileName);
            String uuid = io.getFileContent(file);
            if (TextUtils.isEmpty(uuid)) {
                io.writeData("ABCDEFG");
            }
        }).start();*/

    public final String fileName = "EMS_UUID.txt";

    public String getSDPath() {
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            return Environment.getExternalStorageDirectory().toString();
        } else {
            return "";
        }
    }

    /**
     * 文件的路径名称
     *
     * @return
     */
    public String getFilePath() {
        String sdCardPath = getSDPath();
        if (TextUtils.isEmpty(sdCardPath)) {
            return "";
        } else {
            return sdCardPath + File.separator + "EMS/";
        }
    }

    /**
     * 写入文件
     *
     * @param strcontent
     */
    public void writeData(String strcontent) {
        writeTxtToFile(strcontent, getFilePath(), fileName);
    }

    /**
     * 将字符串写入到文本文件中
     *
     * @param strcontent
     * @param filePath
     * @param fileName
     */
    private void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);
        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                LogUtils.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            LogUtils.e("TestFile", "Error on write File:" + e);
        }
    }

    /**
     * 生成文件
     *
     * @param filePath
     * @param fileName
     * @return
     */
    private File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 生成文件夹
     *
     * @param filePath
     */
    private static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            LogUtils.i("error:", e + "");
        }
    }


    /**
     * 读取指定目录下的所有TXT文件的文件内容
     *
     * @param file
     * @return
     */
    public String getFileContent(File file) {
        StringBuilder content = new StringBuilder();
        if (!file.isDirectory()) {//检查此路径名的文件是否是一个目录(文件夹)
            if (file.getName().endsWith("txt")) {//文件格式为""文件
                try {
                    InputStream instream = new FileInputStream(file);
                    if (null != instream) {
                        InputStreamReader inputreader = new InputStreamReader(instream, StandardCharsets.UTF_8);
                        BufferedReader buffreader = new BufferedReader(inputreader);
                        String line = "";
                        //分行读取
                        while ((line = buffreader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                        instream.close();//关闭输入流
                    }
                } catch (java.io.FileNotFoundException e) {
                    LogUtils.d("TestFile", "The File doesn't not exist.");
                } catch (IOException e) {
                    LogUtils.d("TestFile", e.getMessage());
                }
            }
        }
        return content.toString();
    }

    /**
     * 删除已存储的文件
     * io.deletefile(io.fileName);
     */
    public void deletefile(String fileName) {
        try {
            // 找到文件所在的路径并删除该文件
            File file = new File(getFilePath(), fileName);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
