package com.letv.jr.common.util;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;

import static com.letv.jr.common.util.FileUtil.createFile;

/**
 * file cache util
 * Created by root on 16-11-30.
 * @author zhangzhenzhong
 */

public class FileCacheUtil {


    /**
     * SD卡根目录,Sdcard分为内置和外插，现在手机一般会有内置SdCard，但仍按Sdcard处理
     **/
    private static final String EXTERNAL_SD_PATH = Environment
            .getExternalStorageDirectory() + File.separator + "Lefinancial" + File.separator;
    /**
     * 内部存储目录
     * 目录结构 data/data/<packagename>/filename
     **/
    private static final String INNER_PATH = Environment
            .getDataDirectory() + File.separator;
    /**
     * 是否缓存在内部存储器中
     **/
    public static final boolean SAVE_TO_INNER_PATH = false;


    private static Handler mHandler= new Handler();

    public FileCacheUtil() {
    }

    /**
     * 取出数据
     * @param context
     * @param fileName 只说文件名不要路径
     * @param cacheCallBack 取出文件数据时的监听
     */
    public static void getData(final Context context, final String fileName, final FileCacheCallBack cacheCallBack) {
        new Runnable() {
            @Override
            public void run() {
                try {
                    JudgeGetData(context, fileName, cacheCallBack);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * 保存的数据
     * @param context
     * @param fielName 只说文件名不要路径
     * @param data 需要保存的数据
     */
    public static void saveData(final Context context, final String fielName, final String data) {
       new Runnable() {
            @Override
            public void run() {
                try {
                    JudgeSaveData(context, fielName, data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * 从外部存储中存储String数据
     *
     * @param context
     * @param fielName
     * @param data
     * @return
     * @throws IOException
     */
    public static synchronized boolean JudgeSaveData(Context context, String fielName, String data) throws IOException {
        if (!SAVE_TO_INNER_PATH && FileUtil.hasSdcard()) {
            return writeExternalSDCardFile(context, fielName, data);
        } else {
            return saveDataToFile(context, fielName, data);
        }
    }

    /**
     * 从内部存储中读取String数据
     *
     * @param context
     * @param fileName
     * @return
     * @throws IOException
     */
    public static synchronized void JudgeGetData(Context context, String fileName, final FileCacheCallBack cacheCallBack) throws IOException {
        if (!SAVE_TO_INNER_PATH && FileUtil.hasSdcard()) {
            readExternalSDCardFile(context, fileName, cacheCallBack);
        } else {
            getDataFromFile(context, fileName, cacheCallBack);
        }
    }
    /**
     * 向外部存储器中写入String型数据
     *
     * @param context
     * @param fileName
     * @param data
     * @return
     * @throws IOException
     */
    private static boolean writeExternalSDCardFile(Context context, String fileName, String data) throws IOException {
        if (TextUtils.isEmpty(fileName))
            return false;
        if (TextUtils.isEmpty(data))
            return false;
        FileWriter fileWriter = null;
        if (!FileUtil.makeDirs(EXTERNAL_SD_PATH)) {
            return false;
        }
        String filePath = EXTERNAL_SD_PATH + strToMD5(fileName);
        try {
            createFile(filePath);
            fileWriter = new FileWriter(filePath, false);
            fileWriter.write(data);
            fileWriter.flush();
            return true;
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 向内部存储器中写入String型数据
     *
     * @param context
     * @param fileName
     * @param data
     * @return
     */
    private static boolean saveDataToFile(Context context, String fileName, String data) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        if (TextUtils.isEmpty(data)) {
            return false;
        }
        fileName = strToMD5(fileName);

        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            String str = context.getFilesDir().getPath();
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(data);
            bufferedWriter.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStreamWriter != null) {
                try {
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取外部存储卡中String类型的数据
     *
     * @param context
     * @param fileName
     * @return
     * @throws IOException
     */
    private static String readExternalSDCardFile(Context context, String fileName, final FileCacheCallBack cacheCallBack) {
        if (TextUtils.isEmpty(fileName)) {
            cacheCallBack.onfail();
            return null;
        }
        String filePath = EXTERNAL_SD_PATH + strToMD5(fileName);
        if (!FileUtil.isFileExist(filePath)) {
            cacheCallBack.onfail();
            return null;
        }
        String charsetName = "utf-8";
        File file = new File(filePath);
        StringBuilder fileContent = new StringBuilder("");
        if (file == null || !file.isFile())
            return null;
        BufferedReader reader = null;
        InputStreamReader inputStreamReader=null;
        try {
            inputStreamReader = new InputStreamReader(new FileInputStream(
                    file), charsetName);
            reader = new BufferedReader(inputStreamReader);
            String line = null;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line);
            }
            cacheCallBack.onFinish(fileContent.toString());
            return fileContent.toString();
        } catch (IOException ie) {
            cacheCallBack.onfail();
            ie.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取内部存储卡中String类型的数据
     *
     * @param context
     * @param fileName
     * @return
     */
    private static String getDataFromFile(Context context, String fileName,final FileCacheCallBack cacheCallBack) {
        if (TextUtils.isEmpty(fileName)) {
            cacheCallBack.onfail();
            return null;
        }
        fileName = strToMD5(fileName);
        String filePath = context.getFilesDir().getPath() + File.separator + fileName;
        if (!FileUtil.isFileExist(filePath)) {
            cacheCallBack.onfail();
            return null;
        }
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = null;
        String line = null;
        try {
            stringBuilder = new StringBuilder();
            fileInputStream = context.openFileInput(fileName);
            inputStreamReader = new InputStreamReader(fileInputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            cacheCallBack.onFinish(stringBuilder.toString());
            return stringBuilder.toString();
        } catch (Exception e) {
            cacheCallBack.onfail();
            e.printStackTrace();
            return null;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 文件名称统一为16位MD5保存和查询
     *
     * @param s
     * @return
     */
    private static String strToMD5(String s) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * get file status
     */
    public interface FileCacheCallBack {
        public void onFinish(String str);

        public void onfail();
    }
}
