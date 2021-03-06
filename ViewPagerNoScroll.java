package com.letv.jr.demanddeposit.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 
 * 可控制Viewpager是否可以横向切换
 * @author zhangzhenzhong
 * @version 1.0
 * */
public class ViewPagerNoScroll extends ViewPager {
	private boolean isCanScroll = true;

	public ViewPagerNoScroll(Context context) {
		super(context);
	}

	public ViewPagerNoScroll(Context context, AttributeSet attrs){
		super(context, attrs);
	}
	public void setScanScroll(boolean isCanScroll) {
		this.isCanScroll = isCanScroll;
	}

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		if (isCanScroll) {
			return super.onTouchEvent(arg0);
		} else {
			return false;
		}
	}

	@Override
	public void setCurrentItem(int item, boolean smoothScroll) {
		super.setCurrentItem(item, smoothScroll);
	}

	@Override
	public void setCurrentItem(int item) {
		super.setCurrentItem(item);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if (isCanScroll) {
			return super.onInterceptTouchEvent(arg0);
		} else {
			return false;
		}

	}

	public boolean isCanScroll() {
		return isCanScroll;
	}

	public void setCanScroll(boolean canScroll) {
		isCanScroll = canScroll;
	}
}
