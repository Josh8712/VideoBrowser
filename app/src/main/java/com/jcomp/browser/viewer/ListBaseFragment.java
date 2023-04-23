package com.jcomp.browser.viewer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

public class ListBaseFragment extends Fragment {
    int lastPosition = -1;
    int selectedPosition = -1;
    private static final String LAST_POST_POSITION = "LAST_POST_POSITION";
    private static final String LAST_PLAYING_POSITION = "LAST_PLAYING_POSITION";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            lastPosition = savedInstanceState.getInt(LAST_POST_POSITION, -1);
            selectedPosition = savedInstanceState.getInt(LAST_PLAYING_POSITION, -1);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LAST_POST_POSITION, lastPosition);
        outState.putInt(LAST_PLAYING_POSITION, selectedPosition);
    }

    public boolean canGoBack() {
        return false;
    }

    public void goBack() {

    }

    public void goHome() {

    }
}
