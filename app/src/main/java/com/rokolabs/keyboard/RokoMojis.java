package com.rokolabs.keyboard;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

public class RokoMojis extends AppCompatActivity {
    public final static String SERVICE_NAME = "com.rokolabs.keyboard.KeyboardService";
    private static final String TAG = "RokoMoji";
    ViewPager pager;
    PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.roko_mojis_activity);
        pager = (ViewPager) findViewById(R.id.mojis_pager);
        pagerAdapter = new RmPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
    }

    public void btSkip(View view) {
        pager.setCurrentItem(pager.getCurrentItem() + 1);
    }

    public void btOpenSetting(View view) {
        if (rokoMojiEnabled()) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showInputMethodPicker();
        } else {
            startActivity(new Intent("android.settings.INPUT_METHOD_SETTINGS"));
        }
    }


    private boolean rokoMojiEnabled() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> imList = imm.getEnabledInputMethodList();
        for (InputMethodInfo imi : imList) {
            if (SERVICE_NAME.equalsIgnoreCase(imi.getServiceName())) {
                return true;
            }
        }
        return false;
    }

    public void btOpenLink(View view) {
        switch (view.getId()) {
            case R.id.doc_link_1:
                // About ROKO Stickers
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.roko.mobi/stickers")));
                break;
            case R.id.doc_link_2:
                // How to install
                pager.setCurrentItem(0);
                break;
            case R.id.doc_link_4:
                // Contact Us
                sendMail();
                break;
            case R.id.doc_link_5:
                // Privacy Policy
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.roko.mobi/privacy")));
                break;
            default:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.roko.mobi")));
                break;
        }

    }

    private void sendMail() {
        String mailto = "mailto:info@rokolabs.com" +
                "?subject=" + Uri.encode("ROKOmoji Android Keyboard") +
                "&body=" + Uri.encode("");

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse(mailto));
        startActivity(Intent.createChooser(emailIntent, "Send Email"));
    }


    private class RmPagerAdapter extends FragmentPagerAdapter {

        public RmPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return RmPageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 3;
        }

    }


}
