package com.jcomp.browser.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.customview.widget.Openable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class MyBottomDrawer extends LinearLayout implements Openable {
    BottomSheetBehavior<View> bottomSheetBehavior;

    public MyBottomDrawer(Context context) {
        super(context);
    }

    public MyBottomDrawer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyBottomDrawer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyBottomDrawer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init() {
        bottomSheetBehavior = BottomSheetBehavior.from(this);
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            bottomSheetBehavior.setPeekHeight(TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics()));
        }
    }

    @Override
    public boolean isOpen() {
        return bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED;
    }

    @Override
    public void open() {
        if (isOpen())
            close();
        else
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void close() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public BottomSheetBehavior<View> getBottomSheetBehavior() {
        return bottomSheetBehavior;
    }
}
