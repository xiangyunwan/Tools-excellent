package com.letv.jr.tinker.util;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.letv.jr.common.util.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 功能说明： </br>
 *
 * @author: zhangzhenzhong
 * @version: 1.0
 * @date: 2016/12/28
 * @Copyright (c) 2016. zhangzhenzhong Inc. All rights reserved.
 */
public class DownloaderUtil {
    private Context mContext;
    public DownloaderUtil(Context context){
        this.mContext=context;
    }
//    ProgressBar mProgressBar;
    // 创建okHttpClient对象
    OkHttpClient mOkHttpClient = new OkHttpClient();
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    int progress = msg.arg1;
//                    mProgressBar.setProgress(progress);
                    break;
                case 2:
                    Toast.makeText(mContext,"patch下载完成", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void getFile(final String tinkerVersion) {
        Request request = new Request.Builder().url("http://7xia7w.com1.z0.glb.clouddn.com/fix.apatch").build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("h_bl", "onFailure");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                String SDPath = Environment.getExternalStorageDirectory()+"/tinkers/";
                if (!FileUtil.makeDirs(SDPath)) {
                    return;
                }
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(SDPath, tinkerVersion+".apk");
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        Log.d("h_bl", "progress=" + progress);
                        Message msg = mHandler.obtainMessage();
                        msg.what = 1;
                        msg.arg1 = progress;
                        mHandler.sendMessage(msg);
                    }
                    fos.flush();
                    Log.d("h_bl", "文件下载成功");
                    Message msg = mHandler.obtainMessage();
                    msg.what = 2;
                    msg.obj =tinkerVersion;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    Log.d("h_bl", "文件下载失败");
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
    }
}
