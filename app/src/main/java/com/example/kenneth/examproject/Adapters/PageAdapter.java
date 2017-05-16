package com.example.kenneth.examproject.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

import com.example.kenneth.examproject.DayFragment;
import com.example.kenneth.examproject.MonthFragment;
import com.example.kenneth.examproject.R;
import com.example.kenneth.examproject.WeekFragment;

import static com.example.kenneth.examproject.R.string.dayTitle;

/**
 * Created by Kenneth on 16-05-2017.
 */

//sources:
// https://github.com/codepath/android_guides/wiki/ViewPager-with-FragmentPagerAdapter

public class PageAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 3;
    Context context;

    public PageAdapter(FragmentManager fragmentManager, Context c) {
        super(fragmentManager);
        context = c;
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: // Fragment # 0 - This will show FirstFragment
                return new DayFragment();
            case 1: // Fragment # 0 - This will show FirstFragment different title
                return new WeekFragment();
            case 2: // Fragment # 1 - This will show SecondFragment
                return new MonthFragment();
            default:
                return null;
        }
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {

        if (position == 0) {
            return context.getResources().getString(R.string.dayTitle);
        }
        if (position == 1) {
            return context.getResources().getString(R.string.weekTitle);
        }
        if (position == 2) {
            return context.getResources().getString(R.string.monthTitle);
        }
        return null;
    }
}

