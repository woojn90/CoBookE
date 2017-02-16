package com.android.woojn.coursebookmarkapplication.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.android.woojn.coursebookmarkapplication.fragment.CourseFragment;
import com.android.woojn.coursebookmarkapplication.fragment.ItemFragment;

/**
 * Created by wjn on 2017-02-16.
 */

public class PagerAdapter extends FragmentPagerAdapter {

    private int mTabCount;

    public PagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.mTabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        if (position < 0 || position >= mTabCount) {
            return null;
        }
        switch (position) {
            case 0:
                return new CourseFragment();
            case 1:
                return new ItemFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mTabCount;
    }
}
