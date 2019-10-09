package com.giftsmile.app.smile.Helper;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.giftsmile.app.smile.Fragments.TrainingFragment;
import com.giftsmile.app.smile.Fragments.DonationFragment;
import com.giftsmile.app.smile.Fragments.ServiceFragment;

public class MainViewPagerAdapter extends FragmentPagerAdapter {

    public MainViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position)
        {
            case 0:return new ServiceFragment();
            case 1:return new TrainingFragment();
            case 2:return new DonationFragment();
            default : return new ServiceFragment();
        }

    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position)
        {
            case 0 : return "Services";
            case 1 : return "Trainings";
            case 2 : return "Donation";
            default: return "Services";
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
