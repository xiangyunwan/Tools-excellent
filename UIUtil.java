package com.letv.jr.base.common.util;

import android.view.View;

import com.letv.jr.base.common.dataservice.GlobalData;

/**
 * 屏幕适配
 */
public class UIUtil {

    /**
     * 根据手机的分辨率从 sp 的单位 转成为 px(像素)
     */
    public static int sp2px(float spValue) {
        final float fontScale = GlobalData.getInstance().getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int px2sp(float pxValue) {
        final float fontScale = GlobalData.getInstance().getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * @Description 根据手机的分辨率从 px(像素) 的单位 转成为 dip
     * @param pxValue
     *            需要转换的像素值
     * @return int 转化后的dip值
     */
    public static int pxToDip(float pxValue) {
        final float scale =GlobalData.getInstance().getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(float dpValue) {
        final float scale = GlobalData.getInstance().getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * @param dipValue dp 值
     * @return 返回更精确的结果
     */
    public static float dipToPxFloat(float dipValue) {
        final float scale = GlobalData.getInstance().getContext().getResources().getDisplayMetrics().density;
        return dipValue * scale;
    }

    /**
     * 根据手机的分辨率从 px 的单位 转成为 dip(像素)
     */
    public static int convertPxOrDip(int px) {
        float scale = GlobalData.getInstance().getContext().getResources().getDisplayMetrics().density;
        return (int)(px/scale + 0.5f*(px>=0?1:-1));
    }

    public static void showView(View...views){
        visibility(View.VISIBLE, views);
    }

    public static void hideView(View ...views){
        visibility(View.GONE, views);
    }

    public static void visibility(int visibility, View ...views){

        for(View view:views){
            if(view.getVisibility() != visibility){
                view.setVisibility(visibility);
            }
        }
    }
}
