package com.jcomp.browser.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.jcomp.browser.tools.HelperFunc;


public class HideEditText extends AppCompatEditText implements View.OnFocusChangeListener {
    public HideEditText(@NonNull Context context) {
        super(context);
        init();
    }

    public HideEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HideEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        setOnFocusChangeListener(this);
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (!b) {
            HelperFunc.hideKeyboard(view);
        }
    }
}
