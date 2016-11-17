package com.letv.jr.base.common.helper;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;


public class FragmentHelper {

    private List<Fragment> mFragmentList = new ArrayList<>();
    private boolean isCached = false;
    private FragmentManager mFragmentManager;
    private int mLayoutId;
    private int mIndex = 0;

    public FragmentHelper setFragmentList(List<Fragment> list) {
        if (list != null && list.size() > 0) {
            mFragmentList.addAll(list);
        }
        return this;
    }

    public FragmentHelper addFragment(Fragment fragment) {
        mFragmentList.add(fragment);
        return this;
    }

    public FragmentHelper setLayoutId(int layoutId) {
        this.mLayoutId = layoutId;
        return this;
    }

    public FragmentHelper setCached(boolean cached) {
        isCached = cached;
        return this;
    }

    public void show() {
        setCurrentItem(mIndex);
    }

    public void setCurrentItem(int index) {
        mIndex = index;
        if (isCached) {
            fragmentCache(index);
        } else {
            fragmentNocache(index);
        }

        mIndex = index;
    }

    public FragmentHelper setFragmentManager(FragmentManager manager) {
        this.mFragmentManager = manager;
        return this;
    }


    public int getCurrentItem() {
        return mIndex;
    }

    private void fragmentNocache(int index) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(mLayoutId, mFragmentList.get(index));
        ft.commit();
    }

    private void fragmentCache(int index) {

        //先把Fragment Resume
        Fragment fragment = mFragmentList.get(index);

        if (fragment.isAdded()) {
            fragment.onResume();
        } else {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.add(mLayoutId, fragment);
            ft.commit();
        }

        //再让Fragment到前台显示
        changeFragment(index);
    }

    private void changeFragment(int index) {
        for (int i = 0; i < mFragmentList.size(); i++) {
            Fragment fragment = mFragmentList.get(i);
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            if (index == i) {
                ft.show(fragment);
            } else {
                ft.hide(fragment);
            }
            ft.commit();
        }
    }
}
