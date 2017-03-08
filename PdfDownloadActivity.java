package com.letv.jr.base.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.jr.base.R;
import com.letv.jr.base.common.util.JRLogUtil;
import com.letv.jr.base.view.CustomTitleView;
import com.letv.net.request.UpAndDownLoadBuilder;

import java.io.File;


/**
 * 对于协议是pdf的文件下载页面
 * Created by Yiheng Yan on 16-7-18.
 * yanyiheng@le.com
 * Copyright  2016年 LetvFinancial. All rights reserved.
 */
public class PdfDownloadActivity extends BaseAppCompatActivity implements View.OnClickListener {

    CustomTitleView mTitleBar;
    ProgressBar mProgressBar;
    TextView mTvOpen, mTvName;

    private String mSavePath, sdPath;
    private int progress;
    private static final String TEMP_KEY_TITLE = "temp_key_title";
    private static final String TEMP_KEY_URL = "temp_key_url";
    private static final String TEMP_KEY_FLAG = "temp_key_flag";
    private String mTitle;
    private String mUrl;
    private String mActionFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mTitle = getIntent().getStringExtra(TEMP_KEY_TITLE);
            mUrl = getIntent().getStringExtra(TEMP_KEY_URL);
            mActionFlag = getIntent().getStringExtra(TEMP_KEY_FLAG);
        } else {
            mTitle = savedInstanceState.getString(TEMP_KEY_TITLE);
            mUrl = savedInstanceState.getString(TEMP_KEY_URL);
            mActionFlag = savedInstanceState.getString(TEMP_KEY_FLAG);
        }
        setContentView(R.layout.activity_pdf_download);
        initView();
    }

    private void initView() {
        mTitleBar = (CustomTitleView) findViewById(R.id.titleBar);
        mProgressBar = (ProgressBar) findViewById(R.id.pdf_pb_download);
        mTvOpen = (TextView) findViewById(R.id.pdf_tv_open);
        mTvName = (TextView) findViewById(R.id.pdf_tv_name);

        mTvOpen.setOnClickListener(this);
        mTitleBar.setTitle("文件预览");

        sdPath = Environment.getExternalStorageDirectory() + "/download/";
        String name = mTitle + ".pdf";
        mSavePath = sdPath + name;

        mTvName.setText(name);

        File file = new File(mSavePath);
//        if (file.exists()) {
//            mProgressBar.setVisibility(View.GONE);
//            mTvOpen.setVisibility(View.VISIBLE);
//        } else {
//            downloadApk("http://www8.cao.go.jp/okinawa/8/2012/0409-1-1.pdf");
            downloadApk(mUrl);
//        }
    }

    public static Intent newIntent(Context context, String title, String url, String ActionFlag) {
        Intent intent = new Intent(context, PdfDownloadActivity.class);
        JRLogUtil.e("xiaoxi", "==title=" + title + "==url==" + url + "==ActionFlag=" + ActionFlag);
        intent.putExtra(TEMP_KEY_TITLE, title);
        intent.putExtra(TEMP_KEY_URL, url);
        intent.putExtra(TEMP_KEY_FLAG, ActionFlag);
        return intent;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.pdf_tv_open) {
            openPDFReader();
        }
    }

    /**
     * 下载pdf文件
     */
    private void downloadApk(String downloadUrl) {
        UpAndDownLoadBuilder.downFile(downloadUrl, sdPath,mTitle + ".pdf", new UpAndDownLoadBuilder.OnResponseCallBack() {
            @Override
            public void onProgress(long down, long size) {
                // 计算进度条位置
                progress = (int) (((float) down / size) * 100);
                mProgressBar.setProgress(progress);
            }

            @Override
            public void onSuccess(String result) {
                mProgressBar.setVisibility(View.GONE);
                mTvOpen.setVisibility(View.VISIBLE);
                mTvOpen.setText("用其他应用打开");
            }

            @Override
            public void onFailure() {
                mProgressBar.setVisibility(View.GONE);
                mTvOpen.setVisibility(View.VISIBLE);
                mTvOpen.setText("下载失败,点击重新下载");
            }
        });
    }

    public void openPDFReader() {
        File file = new File(mSavePath);
        if (file.exists()) {
            Uri path = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(path, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(PdfDownloadActivity.this,
                        "该设备暂未安装打开pdf文件的软件,请先下载",
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            mTvOpen.setVisibility(View.GONE);
//            downloadApk("http://www8.cao.go.jp/okinawa/8/2012/0409-1-1.pdf");
            downloadApk(mUrl);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TEMP_KEY_TITLE, mTitle);
        outState.putString(TEMP_KEY_URL, mUrl);
        outState.putString(TEMP_KEY_FLAG, mActionFlag);
    }
}
