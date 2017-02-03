package com.rokolabs.keyboard;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.util.List;

/**
 * Created by mist on 19.12.16.
 */


public class RmPageFragment extends Fragment {
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private static final String TAG = "RmPageFragment";
    int pageNumber;
    TextView textButtonSetting;

    static RmPageFragment newInstance(int page) {
        RmPageFragment pageFragment = new RmPageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;

        switch (pageNumber) {
            case 1:
                view = inflater.inflate(R.layout.roko_mojis_p1, null);
                textButtonSetting = (TextView) view.findViewById(R.id.button_txt);
                break;
            case 2:
                view = inflater.inflate(R.layout.roko_mojis_p3, null);
                break;
            default:
                view = inflater.inflate(R.layout.roko_mojis_p0, null);
                break;
        }
        return view;
    }

    public void onResume() {
        super.onResume();
        if (textButtonSetting != null) {
            if (rokoMojiEnabled()) {
                textButtonSetting.setText(getResources().getString(R.string.selectROKOmoji));
            } else {
                textButtonSetting.setText(getResources().getString(R.string.enableROKOmoji));
            }
        }
    }

    private boolean rokoMojiEnabled() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> imList = imm.getEnabledInputMethodList();
        for (InputMethodInfo imi : imList) {
            if (RokoMojis.SERVICE_NAME.equalsIgnoreCase(imi.getServiceName())) {
                return true;
            }
        }
        return false;
    }
}