package com.rokolabs.keyboard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class RokoMoji extends AppCompatActivity {
    ViewPager pager;
    PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.roko_mojis_activity);
        pager = (ViewPager) findViewById(R.id.mojis_pager);
        pagerAdapter = new RmPagerAdapter(getSupportFragmentManager(), new Fragment[]{
                new RmPageFragment(),
                new EnableKeyboardFragment(),
                new LinksFragment()
        });
        pager.setAdapter(pagerAdapter);
    }

    public void switchToFirstPage() {
        pager.setCurrentItem(0);
    }

    private class RmPagerAdapter extends FragmentPagerAdapter {
        private final Fragment[] fragments;

        RmPagerAdapter(FragmentManager fm, Fragment[] fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }
    }
}
