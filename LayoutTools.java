package com.letv.jr;

import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.letv.jr.base.common.util.UIUtil;
import com.letv.jr.base.common.util.ViewUtil;

/**
 * Created by zhangzhenzhong on 2016/11/18.
 */
public class LayoutTools {


    private void setLinearLayouHeight(){
        LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.rlayout_content);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.height = UIUtil.dip2px(160f);
        linearLayout.setLayoutParams(layoutParams);
    }
    private void setRelaytiveLayouHeight(){
        RelativeLayout relativeLayout = (RelativeLayout)view.findViewById(R.id.rlayout_content);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.height = UIUtil.dip2px(160f);
        relativeLayout.setLayoutParams(layoutParams);
    }

}
