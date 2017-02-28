package com.letv.jr.common.util;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import static com.letv.jr.common.util.FileUtil.createFile;

/**
 * file cache util
 * Created by root on 16-11-30.
 * @author zhangzhenzhong
 */

public class FileCacheUtil {

    private final  static  int READ_CACHE=0x001;

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
     * 对象写入缓存
     * @param context
     * @param obj  必需序列化
     * @param fileName
     * @param <T>
     */
    public static <T> void writeToCache(final Context context, final T obj,
                                  final String fileName) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                String strFileName = strToMD5(fileName);
                String filePath = context.getFilesDir().getPath() + File.separator +strFileName;
                writeDateToFile(obj,filePath );
            }
        });
    }
    /**
     * 读取对象缓存
     * @param context
     * @param fileName
     * @param listener
     * @param <T>
     */
    public static <T> void readCache(final Context context, final String fileName,
                               final FileCacheCallBack<T> listener) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                String strFileName = strToMD5(fileName);
                String filePath = context.getFilesDir().getPath() + File.separator +strFileName;
                final T obj = readDateFromFile(context,filePath,listener);

//                listener.onFinish(fileName ,obj);
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("listener", listener);
                data.put("bean", obj);
                data.put("fileName", strFileName);
                Message msg = mHandler.obtainMessage();
                msg.what = READ_CACHE;
                msg.obj = data;
                // 转给主线程回调
                mMainCallBackHandler.sendMessage(msg);
            }
        });
    }

    public static synchronized <T> void writeDateToFile(T rsls, String filePath) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(rsls);
            byte[] data = baos.toByteArray();
            OutputStream os = new FileOutputStream(new File(filePath));
            os.write(data);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T> T readDateFromFile(Context context,final String filePath,final FileCacheCallBack<T> listener) {
        T obj = null;
        try {

            FileInputStream fis = new FileInputStream(filePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            while (fis.available() > 0) {
                obj = (T) ois.readObject();
            }
            ois.close();
        } catch (Exception e) {
//			e.printStackTrace();
            listener.onfail(e.toString());
        }
        return obj;
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
            cacheCallBack.onfail("fileName is null");
            return null;
        }
        String filePath = EXTERNAL_SD_PATH + strToMD5(fileName);
        if (!FileUtil.isFileExist(filePath)) {
            cacheCallBack.onfail("filePath is null");
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
            cacheCallBack.onfail("fail");
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
            cacheCallBack.onfail("fileName is null");
            return null;
        }
        fileName = strToMD5(fileName);
        String filePath = context.getFilesDir().getPath() + File.separator + fileName;
        if (!FileUtil.isFileExist(filePath)) {
            cacheCallBack.onfail("fail");
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
            cacheCallBack.onfail("fa");
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
     * 主线程回调Handler
     */
    private static Handler mMainCallBackHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final FileCacheCallBack listener = (FileCacheCallBack) ((Map) msg.obj).get("listener");
            final Object bean = (Object) ((Map) msg.obj).get("bean");
            final String fileName = String.valueOf(((Map) msg.obj).get("fileName"));
            switch (msg.what) {
                case READ_CACHE:
                    if (null == listener){
                        listener.onfail("listener为空");
                        return;
                    }else{
                        try {
                            listener.onFinish(fileName,bean);
                        } catch (Exception e) {
                            delFile(fileName);
                            listener.onfail("bean转换失败");
                        }
                    }
                    break;
                default:
                    break;
            }
        };
    };
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
     * 删除对象形式缓存数据
     * @param context
     * @param fileName
     */
    public static void delCacheBeanFile(Context context,String fileName){
        fileName = strToMD5(fileName);
        String filePath = context.getFilesDir().getPath() + File.separator +fileName;
        File file = new File(filePath);
        if(file.exists()){
            file.delete();
        }
    }

    /**
     *删除所有缓存数据
     * @param context
     */
    public static void delAllCacheBeanFile(Context context){
        String filePath = context.getFilesDir().getPath() + File.separator ;
        FileUtil.delete(filePath, new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return false;
            }
        });
    }
    public static void delFile(String filePath){
        File file = new File(filePath);
        if(file.exists()){
            file.delete();
        }
    }
    /**
     * get file status
     */
    public interface FileCacheCallBack<T> {
        void onFinish(String str);

        void onfail(String msg);

        void onFinish(String str ,T t);

    }
}
