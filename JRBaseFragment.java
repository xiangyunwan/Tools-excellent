package com.letv.jr.demanddeposit.view;

import com.letv.jr.base.fragment.BaseAppFragment;

/**
 * 懒加载Fragment
 * @author zhangzhenzhong
 *
 */
public abstract class JRBaseFragment extends BaseAppFragment {

	/** Fragment当前状态是否可见 */
	protected boolean mIsVisible;
	/**
	 * 页面是否初始化完毕
	 */
	protected boolean isLoaded = false;
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (getUserVisibleHint()) {
			mIsVisible = true;
			onVisible();
		} else {
			mIsVisible = false;
			onInvisible();
		}
	}
	/**
	 * 可见
	 */
	protected void onVisible() {
		lazyLoad();
	}
	/**
	 * 不可见
	 */
	protected void onInvisible() {

	}
	/**
	 * 延迟加载 子类必须重写此方法
	 */
	protected abstract void lazyLoad();
}
