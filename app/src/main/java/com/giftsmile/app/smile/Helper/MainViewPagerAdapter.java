package com.giftsmile.app.smile.Helper;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.RecyclerView;

import com.giftsmile.app.smile.Fragments.AddFragment;
import com.giftsmile.app.smile.Fragments.PeopleFragment;
import com.giftsmile.app.smile.Fragments.SearchFragment;

public class MainViewPagerAdapter extends FragmentPagerAdapter {

    public MainViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position)
        {
            case 0:return new SearchFragment();
            case 1:return new AddFragment();
            case 2:return new PeopleFragment();
            default : return new SearchFragment();
        }

    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position)
        {
            case 0 : return "Search";
            case 1 : return "Add";
            case 2 : return "People";
            default: return "Search";
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
