package com.jcomp.browser.history;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jcomp.browser.R;
import com.jcomp.browser.history.db.History;

public class HistorySetting extends History {
    public static final String SETTING_URL = "SETTING_URL";

    public HistorySetting(Context context) {
        super(context.getString(R.string.setting), SETTING_URL);
        this.removable = false;
    }

    @Override
    public void setIcon(ImageView button, TextView textView) {
        button.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
        button.setImageResource(R.drawable.baseline_settings_24);
    }
}
