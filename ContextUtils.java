package com.letv.jr;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.letv.jr.base.activity.GestureManageActivity;
import com.letv.jr.base.activity.GesturePwUnLockActivity;
import com.letv.jr.base.common.Constant;
import com.letv.jr.base.common.dataservice.UserCenter;
import com.letv.jr.base.common.util.SharePrefrenceUtil;
import com.letv.jr.base.model.UserInformationBean;
import com.letv.jr.welcomePage.AdvertisementActivity;
import com.letv.jr.welcomePage.GuidancePageActivity;
import com.letv.jr.welcomePage.SplashActivity;

import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * 功能说明： </br>
 *
 * @author: zhangzhenzhong
 * @version: 1.0
 * @date: 2017/3/8
 * @Copyright (c) 2017. zhangzhenzhong Inc. All rights reserved.
 */
public class ContextUtils {
    /**
     * 程序是否在前台运行
     *
     * @return
     */
    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    //调用方式：myApplication.registerActivityLifecycleCallbacks(callBacks);
    Application.ActivityLifecycleCallbacks callBacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
        }
        @Override
        public void onActivityStarted(Activity activity) {
        }
        @Override
        public void onActivityResumed(Activity activity) {
        }
        @Override
        public void onActivityPaused(Activity activity) {
        }
        @Override
        public void onActivityStopped(Activity activity) {
        }
        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }
        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    };
}
