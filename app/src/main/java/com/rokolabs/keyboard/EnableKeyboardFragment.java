package com.rokolabs.keyboard;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.rokolabs.rokomoji.KeyboardService;

public class EnableKeyboardFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.roko_mojis_p1, null);
        view.findViewById(R.id.button_txt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (KeyboardService.rokomojiEnabled(getActivity())) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showInputMethodPicker();
                } else {
                    startActivity(new Intent("android.settings.INPUT_METHOD_SETTINGS"));
                }
            }
        });
        return view;
    }

    public void onResume() {
        super.onResume();
        TextView textView = (TextView) getView().findViewById(R.id.button_txt);
        if (textView != null) {
            if (KeyboardService.rokomojiEnabled(getActivity())) {
                textView.setText(getResources().getString(R.string.selectROKOmoji));
            } else {
                textView.setText(getResources().getString(R.string.enableROKOmoji));
            }
        }
    }
}
