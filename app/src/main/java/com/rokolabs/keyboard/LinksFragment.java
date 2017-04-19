package com.rokolabs.keyboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LinksFragment extends Fragment implements View.OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.roko_mojis_p2, null);
        view.findViewById(R.id.doc_link_1).setOnClickListener(this);
        view.findViewById(R.id.doc_link_2).setOnClickListener(this);
        view.findViewById(R.id.doc_link_4).setOnClickListener(this);
        view.findViewById(R.id.doc_link_5).setOnClickListener(this);
        return view;
    }

    private void sendMail() {
        String mailto = "mailto:" + getResources().getString(R.string.email_mailto) +
                "?subject=" + Uri.encode(getResources().getString(R.string.email_subject)) +
                "&body=" + Uri.encode("");

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse(mailto));
        startActivity(Intent.createChooser(emailIntent, "Send Email"));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.doc_link_1:
                // About ROKO Stickers
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.roko.mobi/stickers")));
                break;
            case R.id.doc_link_2:
                // How to install
                ((RokoMoji) getActivity()).switchToFirstPage();
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
}
